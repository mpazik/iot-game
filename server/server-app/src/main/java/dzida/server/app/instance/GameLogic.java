package dzida.server.app.instance;

import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.npc.AiService;
import dzida.server.app.instance.scenario.NpcScenarioLogic;
import dzida.server.app.instance.scenario.OpenWorldScenarioLogic;
import dzida.server.app.instance.scenario.ScenarioLogic;
import dzida.server.app.instance.scenario.SurvivalScenarioLogic;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.core.Scheduler;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.scenario.SurvivalScenarioFactory;

import java.util.Optional;
import java.util.function.Consumer;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private final InstanceStateManager state;
    private final AiService aiService;
    private final ScenarioLogic scenarioLogic;
    private final Scheduler scheduler;
    private final Consumer<InstanceCommand> commandConsumer;

    public GameLogic(
            Scheduler scheduler,
            InstanceStateManager state,
            Optional<SurvivalScenarioFactory.SurvivalScenario> survivalScenario,
            Scenario scenario,
            AiService aiService,
            Consumer<InstanceCommand> commandConsumer) {
        this.state = state;
        this.aiService = aiService;
        this.scheduler = scheduler;
        this.commandConsumer = commandConsumer;

        NpcScenarioLogic npcScenarioLogic = new NpcScenarioLogic(aiService, state, commandConsumer);

        if (survivalScenario.isPresent()) {
            this.scenarioLogic = new SurvivalScenarioLogic(scheduler, state, npcScenarioLogic, (Survival) scenario, survivalScenario.get(), commandConsumer);
        } else {
            this.scenarioLogic = new OpenWorldScenarioLogic();
        }
    }

    public void start() {
        scheduler.schedulePeriodically(this::aiTick, 500, 500);
        scenarioLogic.start();
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Character character = state.getCharacterService().getCharacter(event.characterId);
            if (character instanceof PlayerCharacter) {
                // player may died because logout it's something that have to be kept in mind
                scenarioLogic.handlePlayerDead(event.characterId);
            } else {
                scenarioLogic.handleNpcDead(event.characterId);
            }
        });
    }

    private void aiTick() {
        aiService.processTick().forEach(commandConsumer);
    }

}
