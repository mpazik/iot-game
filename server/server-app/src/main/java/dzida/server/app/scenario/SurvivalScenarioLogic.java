package dzida.server.app.scenario;

import com.google.common.collect.ImmutableList;
import dzida.server.app.GameEventDispatcher;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.Npc;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.CharacterId;
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
    private final GameEventDispatcher gameEventDispatcher;
    private final SurvivalScenarioState survivalScenarioState;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final Scheduler scheduler;

    public SurvivalScenarioLogic(
            Scheduler scheduler,
            GameEventDispatcher gameEventDispatcher,
            NpcScenarioLogic npcScenarioLogic,
            Survival survival,
            SurvivalScenario survivalScenario,
            CharacterService characterService,
            PlayerService playerService) {
        this.scheduler = scheduler;
        this.npcScenarioLogic = npcScenarioLogic;
        this.survival = survival;
        this.survivalScenario = survivalScenario;
        this.gameEventDispatcher =gameEventDispatcher;
        this.characterService = characterService;
        this.playerService = playerService;
        survivalScenarioState = new SurvivalScenarioState();
    }

    @Override
    public void handlePlayerDead(CharacterId characterId, Player.Id playerId) {
        if (survivalScenarioState.end) {
            return;
        }
        gameEventDispatcher.dispatchEvent(new ScenarioEnd(Defeat));
        survivalScenarioState.end = true;
    }

    @Override
    public void handleNpcDead(CharacterId characterId) {
        npcScenarioLogic.removeNpc(characterId);
        if (survivalScenarioState.end) {
            return;
        }
        survivalScenarioState.npcDied += 1;
        if (survivalScenarioState.npcDied == survivalScenario.getNumberOfNpcToKill()) {
            victory();
        }
    }

    private void victory() {
        List<PlayerCharacter> players = characterService.getCharactersOfType(PlayerCharacter.class);
        players.stream().forEach(playerCharacter -> {
            Player.Id playerId = playerCharacter.getPlayerId();
            Player.Data player = playerService.getPlayer(playerId).getData();
            if (survivalScenario.getDifficultyLevel() > player.getHighestDifficultyLevel()) {
                Player.Data newPlayerData = player.toBuilder().highestDifficultyLevel(survivalScenario.getDifficultyLevel()).build();
                playerService.updatePlayerData(playerId, newPlayerData);
            }
        });
        survivalScenarioState.end = true;
        gameEventDispatcher.dispatchEvents(ImmutableList.of(new ScenarioEnd(Victory)));
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

        if (survivalScenarioState.spawnedNpc < survivalScenario.getNumberOfNpcToKill()) {
            scheduler.schedule(this::spawnNpc, survivalScenario.getBotSpawnTime());
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
        return spawns.get(index).getPosition();
    }

    private final class SurvivalScenarioState {
        int npcDied = 0;
        int spawnedNpc = 0;
        boolean end = false;
    }
}
