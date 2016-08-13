package dzida.server.app.scenario;

import com.google.common.collect.ImmutableList;
import dzida.server.app.InstanceStateManager;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.Npc;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.scenario.ScenarioEnd;
import dzida.server.core.scenario.SurvivalScenarioFactory.SurvivalScenario;

import java.util.List;
import java.util.Random;

import static dzida.server.core.scenario.ScenarioEnd.Resolution.Defeat;
import static dzida.server.core.scenario.ScenarioEnd.Resolution.Victory;

public class SurvivalScenarioLogic implements ScenarioLogic {

    private final NpcScenarioLogic npcScenarioLogic;
    private final Survival survival;
    private final SurvivalScenario survivalScenario;
    private final InstanceStateManager instanceStateManager;
    private final SurvivalScenarioState survivalScenarioState;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final Scheduler scheduler;

    public SurvivalScenarioLogic(
            Scheduler scheduler,
            InstanceStateManager instanceStateManager,
            NpcScenarioLogic npcScenarioLogic,
            Survival survival,
            SurvivalScenario survivalScenario,
            CharacterService characterService,
            PlayerService playerService) {
        this.scheduler = scheduler;
        this.npcScenarioLogic = npcScenarioLogic;
        this.survival = survival;
        this.survivalScenario = survivalScenario;
        this.instanceStateManager = instanceStateManager;
        this.characterService = characterService;
        this.playerService = playerService;
        survivalScenarioState = new SurvivalScenarioState();
    }

    @Override
    public void handlePlayerDead(Id<Character> characterId, Id<Player> playerId) {
        if (survivalScenarioState.end) {
            return;
        }
        instanceStateManager.dispatchEvent(new ScenarioEnd(Defeat));
        survivalScenarioState.end = true;
    }

    @Override
    public void handleNpcDead(Id<Character> characterId) {
        npcScenarioLogic.removeNpc(characterId);
        if (survivalScenarioState.end) {
            return;
        }
        survivalScenarioState.npcDied += 1;
        if (survivalScenarioState.npcDied == survivalScenario.numberOfNpcToKill) {
            victory();
        }
    }

    private void victory() {
        List<PlayerCharacter> players = characterService.getCharactersOfType(PlayerCharacter.class);
        players.forEach(playerCharacter -> {
            Id<Player> playerId = playerCharacter.getPlayerId();
            Player.Data player = playerService.getPlayer(playerId).getData();
            if (survivalScenario.difficultyLevel > player.getHighestDifficultyLevel()) {
                Player.Data newPlayerData = new Player.Data(player.getNick(), survival.getDifficultyLevel(), player.getLastDifficultyLevel());
                playerService.updatePlayerData(playerId, newPlayerData);
            }
        });
        survivalScenarioState.end = true;
        instanceStateManager.dispatchEvents(ImmutableList.of(new ScenarioEnd(Victory)));
    }

    @Override
    public void start() {
        spawnNpc();

    }

    private void spawnNpc() {
        if (survivalScenarioState.end) {
            return;
        }

        survivalScenarioState.spawnedNpc += 1;
        Point randomNpcSpawnPoint = getRandomNpcSpawnPoint();
        npcScenarioLogic.addNpc(randomNpcSpawnPoint, getRandomNpcType());

        if (survivalScenarioState.spawnedNpc < survivalScenario.numberOfNpcToKill) {
            scheduler.schedule(this::spawnNpc, survivalScenario.botSpawnTime);
        }
    }

    private int getRandomNpcType() {
        int[] npcTypes = {Npc.Archer, Npc.Fighter};
        int index = new Random().nextInt(npcTypes.length);
        return npcTypes[index];
    }

    private Point getRandomNpcSpawnPoint() {
        List<Survival.Spawn> spawns = survival.getSpawns();
        int index = new Random().nextInt(spawns.size());
        return spawns.get(index).position;
    }

    private final class SurvivalScenarioState {
        int npcDied = 0;
        int spawnedNpc = 0;
        boolean end = false;
    }
}
