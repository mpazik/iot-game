package dzida.server.app;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.npc.Npc;
import dzida.server.core.CharacterId;
import dzida.server.core.PlayerId;
import dzida.server.core.PlayerService;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Move;
import dzida.server.core.position.model.Position;
import dzida.server.core.scenario.SurvivalScenarioFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private static final int SPAWN_TIME = 4000;

    private final Scheduler scheduler;
    private final PositionService positionService;
    private final CharacterService characterService;
    private final PlayerService playerService;
    private final GameEventDispatcher gameEventDispatcher;
    private final Runnable send;
    private final AiService aiService;
    private final PositionStoreImpl positionStore;
    private final CommandResolver commandResolver;

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
        this.scheduler = scheduler;
        this.gameEventDispatcher = gameEventDispatcher;
        this.characterService = characterService;
        this.positionService = positionService;
        this.playerService = playerService;
        this.send = send;
        this.aiService = aiService;
        this.positionStore = positionStore;
        this.commandResolver = commandResolver;
        if (survivalScenario.isPresent()) {
            runSurvivalScenario((Survival) scenario, survivalScenario.get());
        }

        scheduler.schedulePeriodically(this::aiTick, 500, 500);
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Optional<PlayerCharacter> playerCharacterOpt = characterService.getPlayerCharacter(event.getCharacterId());
            playerCharacterOpt.ifPresent(playerCharacter -> {
                // player may died because logout, so we have to check if he is still logged it.
                PlayerId playerId = playerCharacter.getPlayerId();
                if (playerService.isPlayerPlaying(playerId)) {
                    respawnPlayer(event.getCharacterId(), playerCharacter.getNick(), playerId);
                }
            });
        });
    }

    private void respawnPlayer(CharacterId characterId, String nick, PlayerId playerId) {
        PlayerCharacter newPlayerCharacter = new PlayerCharacter(characterId, nick, playerId);
        Move initialMove = positionService.getInitialMove(characterId);
        scheduleGameEvents(Collections.singletonList(new CharacterSpawned(newPlayerCharacter, initialMove)), SPAWN_TIME);
    }

    private void runSurvivalScenario(Survival survival, SurvivalScenarioFactory.SurvivalScenario scenario) {
        survival.getSpawns().stream().forEach(spawn -> addNpc(spawn.getPosition(), Npc.Fighter));
    }

    private void scheduleGameEvents(List<GameEvent> events, int delay) {
        scheduler.schedule(() -> gameEventDispatcher.dispatchEvents(events), delay);
    }

    private void addNpc(Position position, int npcType) {
        CharacterId characterId = new CharacterId((int) Math.round((Math.random() * 100000)));
        NpcCharacter character = new NpcCharacter(characterId, npcType);
        positionStore.setPosition(characterId, position);
        aiService.createNpc(npcType, character);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, event -> {
            List<GameEvent> gameEvents = aiService.processPacket(characterId, event);
            gameEventDispatcher.dispatchEvents(gameEvents);
        });
    }

    private void aiTick() {
        gameEventDispatcher.dispatchEvents(aiService.processTick());
        send.run();
    }
}
