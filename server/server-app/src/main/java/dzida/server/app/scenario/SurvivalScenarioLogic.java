package dzida.server.app.scenario;

import dzida.server.app.GameEventDispatcher;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.Npc;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.PlayerId;
import dzida.server.core.scenario.ScenarioEnd;
import dzida.server.core.scenario.SurvivalScenarioFactory.SurvivalScenario;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dzida.server.core.scenario.ScenarioEnd.Resolution.Defeat;
import static dzida.server.core.scenario.ScenarioEnd.Resolution.Victory;

public class SurvivalScenarioLogic implements ScenarioLogic {

    private final NpcScenarioLogic npcScenarioLogic;
    private final Survival survival;
    private final SurvivalScenario survivalScenario;
    private final GameEventDispatcher gameEventDispatcher;
    private final SurvivalScenarioState survivalScenarioState;
    private final CharacterService characterService;

    public SurvivalScenarioLogic(
            Scheduler scheduler,
            GameEventDispatcher gameEventDispatcher,
            NpcScenarioLogic npcScenarioLogic,
            Survival survival,
            SurvivalScenario survivalScenario,
            CharacterService characterService) {
        this.npcScenarioLogic = npcScenarioLogic;
        this.survival = survival;
        this.survivalScenario = survivalScenario;
        this.gameEventDispatcher =gameEventDispatcher;
        this.characterService = characterService;
        survivalScenarioState = new SurvivalScenarioState();
    }

    @Override
    public void handlePlayerDead(CharacterId characterId, PlayerId playerId) {
        if (survivalScenarioState.isEnd()) {
            return;
        }
        gameEventDispatcher.dispatchEvent(new ScenarioEnd(Defeat));
        survivalScenarioState.setEnd(true);
    }

    @Override
    public void handleNpcDead(CharacterId characterId) {
        if (survivalScenarioState.isEnd()) {
            return;
        }
        int npcDied = survivalScenarioState.getNpcDied() + 1;
        survivalScenarioState.setNpcDied(npcDied);
        if (npcDied == survival.getSpawns().size()) {

            Stream<CharacterDied> characterDiedStream = characterService.getCharactersOfType(Character.Player).map(Character::getId).map(CharacterDied::new);
            List<GameEvent> messages = Stream.concat(characterDiedStream, Stream.of(new ScenarioEnd(Victory))).collect(Collectors.toList());
            survivalScenarioState.setEnd(true);
            gameEventDispatcher.dispatchEvents(messages);
        }
    }

    @Override
    public void start() {
        survival.getSpawns().stream().forEach(spawn -> npcScenarioLogic.addNpc(spawn.getPosition(), Npc.Fighter));
    }

    @Data
    private final class SurvivalScenarioState {
        int npcDied = 0;
        boolean end = false;
    }
}
