package dzida.server.app;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.scenario.*;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionStore;
import dzida.server.core.scenario.SurvivalScenarioFactory;

import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final InstanceStateManager instanceStateManager;
    private final Runnable send;
    private final AiService aiService;
    private final ScenarioLogic scenarioLogic;
    private final Scheduler scheduler;

    public GameLogic(
            Scheduler scheduler,
            InstanceStateManager instanceStateManager,
            CharacterService characterService,
            PlayerService playerService,
            Optional<SurvivalScenarioFactory.SurvivalScenario> survivalScenario,
            Scenario scenario,
            Runnable send,
            AiService aiService,
            PositionStore positionStore,
            CommandResolver commandResolver,
            CharacterCommandHandler characterCommandHandler) {
        this.instanceStateManager = instanceStateManager;
        this.characterService = characterService;
        this.playerService = playerService;
        this.send = send;
        this.aiService = aiService;
        this.scheduler = scheduler;

        NpcScenarioLogic npcScenarioLogic = new NpcScenarioLogic(aiService, positionStore, commandResolver, instanceStateManager);
        GameEventScheduler gameEventScheduler = new GameEventScheduler(instanceStateManager, scheduler);

        if (survivalScenario.isPresent()) {
            this.scenarioLogic = new SurvivalScenarioLogic(scheduler, instanceStateManager, npcScenarioLogic, (Survival) scenario, survivalScenario.get(), characterService, playerService);
        } else {
            this.scenarioLogic = new OpenWorldScenarioLogic(playerService, gameEventScheduler, characterCommandHandler);
        }
    }

    public void start() {
        scheduler.schedulePeriodically(this::aiTick, 500, 500);
        scenarioLogic.start();
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Optional<PlayerCharacter> playerCharacterOpt = characterService.getPlayerCharacter(event.characterId);
            if (playerCharacterOpt.isPresent()) {
                Id<Player> playerId = playerCharacterOpt.get().getPlayerId();
                // player may died because logout, so we have to check if he is still logged it.
                if (playerService.isPlayerPlaying(playerId)) {
                    scenarioLogic.handlePlayerDead(event.characterId, playerId);
                }
            } else {
                instanceStateManager.unregisterCharacter(event.characterId);
                scenarioLogic.handleNpcDead(event.characterId);
            }
        });
    }

    private void aiTick() {
        instanceStateManager.dispatchEvents(aiService.processTick());
        send.run();
    }

    public void playerJoined(PlayerCharacter character) {
        scenarioLogic.playerJoined(character);
    }
}
