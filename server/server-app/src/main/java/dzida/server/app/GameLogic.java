package dzida.server.app;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.scenario.*;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.PlayerId;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionService;
import dzida.server.core.scenario.SurvivalScenarioFactory;

import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final GameEventDispatcher gameEventDispatcher;
    private final Runnable send;
    private final AiService aiService;
    private final ScenarioLogic scenarioLogic;
    private final Scheduler scheduler;

    public GameLogic(
            Scheduler scheduler,
            GameEventDispatcher gameEventDispatcher,
            PositionService positionService,
            CharacterService characterService,
            PlayerService playerService,
            Optional<SurvivalScenarioFactory.SurvivalScenario> survivalScenario,
            Scenario scenario,
            Runnable send,
            AiService aiService,
            PositionStoreImpl positionStore,
            CommandResolver commandResolver) {
        this.gameEventDispatcher = gameEventDispatcher;
        this.characterService = characterService;
        this.playerService = playerService;
        this.send = send;
        this.aiService = aiService;
        this.scheduler = scheduler;

        NpcScenarioLogic npcScenarioLogic = new NpcScenarioLogic(aiService, positionStore, commandResolver, gameEventDispatcher);
        GameEventScheduler gameEventScheduler = new GameEventScheduler(gameEventDispatcher, scheduler);

        if (survivalScenario.isPresent()) {
            this.scenarioLogic = new SurvivalScenarioLogic(scheduler, gameEventDispatcher, npcScenarioLogic, (Survival) scenario, survivalScenario.get(), characterService);
        } else {
            this.scenarioLogic = new OpenWorldScenarioLogic(positionService, playerService, gameEventScheduler);
        }
    }

    public void start() {
        scheduler.schedulePeriodically(this::aiTick, 500, 500);
        scenarioLogic.start();
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Optional<PlayerCharacter> playerCharacterOpt = characterService.getPlayerCharacter(event.getCharacterId());
            if (playerCharacterOpt.isPresent()) {
                PlayerId playerId = playerCharacterOpt.get().getPlayerId();
                // player may died because logout, so we have to check if he is still logged it.
                if (playerService.isPlayerPlaying(playerId)) {
                    scenarioLogic.handlePlayerDead(event.getCharacterId(), playerId);
                }
            }else {
                scenarioLogic.handleNpcDead(event.getCharacterId());
            }
        });
    }

    private void aiTick() {
        gameEventDispatcher.dispatchEvents(aiService.processTick());
        send.run();
    }
}
