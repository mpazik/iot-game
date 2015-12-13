package dzida.server.app;

import dzida.server.core.CharacterId;
import dzida.server.core.PlayerId;
import dzida.server.core.PlayerService;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Move;

import java.util.Collections;
import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class GameLogic {
    private static final int SPAWN_TIME = 4000;

    private final Scheduler scheduler;
    private final PositionService positionService;
    private final CharacterService characterService;
    private final PlayerService playerService;

    public GameLogic(Scheduler scheduler, PositionService positionService, CharacterService characterService, PlayerService playerService) {
        this.scheduler = scheduler;
        this.characterService = characterService;
        this.positionService = positionService;
        this.playerService = playerService;
    }

    public void processEventBeforeChanges(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterDied.class).then(event -> {
            Optional<PlayerCharacter> playerCharacterOpt = characterService.getPlayerCharacter(event.getCharacterId());
            playerCharacterOpt.ifPresent(playerCharacter -> {
                // player may died because logout
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
        scheduler.scheduleGameEvents(Collections.singletonList(new CharacterSpawned(newPlayerCharacter, initialMove)), SPAWN_TIME);
    }
}
