package dzida.server.app.instance;

import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.npc.AiService;
import dzida.server.app.instance.scenario.InstanceCommandScheduler;
import dzida.server.app.instance.scenario.NpcScenarioLogic;
import dzida.server.app.instance.scenario.OpenWorldScenarioLogic;
import dzida.server.app.instance.scenario.ScenarioLogic;
import dzida.server.app.instance.scenario.SurvivalScenarioLogic;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.scenario.SurvivalScenarioFactory;

import java.util.Optional;
import java.util.function.Consumer;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private final PlayerService playerService;
    private final InstanceStateManager state;
    private final AiService aiService;
    private final ScenarioLogic scenarioLogic;
    private final Scheduler scheduler;
    private final Consumer<InstanceCommand> commandConsumer;

    public GameLogic(
            Scheduler scheduler,
            InstanceStateManager state,
            PlayerService playerService,
            Optional<SurvivalScenarioFactory.SurvivalScenario> survivalScenario,
            Scenario scenario,
            AiService aiService,
            Consumer<InstanceCommand> commandConsumer) {
        this.state = state;
        this.playerService = playerService;
        this.aiService = aiService;
        this.scheduler = scheduler;
        this.commandConsumer = commandConsumer;

        NpcScenarioLogic npcScenarioLogic = new NpcScenarioLogic(aiService, state, commandConsumer);
        InstanceCommandScheduler instanceCommandScheduler = new InstanceCommandScheduler(commandConsumer, scheduler);

        if (survivalScenario.isPresent()) {
            this.scenarioLogic = new SurvivalScenarioLogic(scheduler, state, npcScenarioLogic, (Survival) scenario, survivalScenario.get(), playerService, commandConsumer);
        } else {
            this.scenarioLogic = new OpenWorldScenarioLogic(playerService, instanceCommandScheduler);
        }
    }

    public void start() {
        scheduler.schedulePeriodically(this::aiTick, 500, 500);
        scenarioLogic.start();
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Optional<PlayerCharacter> playerCharacterOpt = state.getCharacterService().getPlayerCharacter(event.characterId);
            if (playerCharacterOpt.isPresent()) {
                Id<Player> playerId = playerCharacterOpt.get().getPlayerId();
                // player may died because logout, so we have to check if he is still logged it.
                if (playerService.isPlayerPlaying(playerId)) {
                    scenarioLogic.handlePlayerDead(event.characterId, playerId);
                }
            } else {
                scenarioLogic.handleNpcDead(event.characterId);
            }
        });
    }

    private void aiTick() {
        aiService.processTick().forEach(commandConsumer);
    }

    public void playerJoined(PlayerCharacter character) {
        scenarioLogic.playerJoined(character);
    }
}
