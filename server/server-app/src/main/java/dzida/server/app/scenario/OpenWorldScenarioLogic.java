package dzida.server.app.scenario;

import dzida.server.core.character.CharacterId;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.PlayerId;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Move;

import static java.util.Collections.singletonList;

public class OpenWorldScenarioLogic implements ScenarioLogic {
    private static final int SPAWN_TIME = 4000;

    private final PositionService positionService;
    private final PlayerService playerService;
    private final GameEventScheduler gameEventScheduler;

    public OpenWorldScenarioLogic(
            PositionService positionService,
            PlayerService playerService,
            GameEventScheduler gameEventScheduler) {
        this.positionService = positionService;
        this.playerService = playerService;
        this.gameEventScheduler = gameEventScheduler;
    }

    @Override
    public void handlePlayerDead(CharacterId characterId, PlayerId playerId) {
        String playerNick = playerService.getPlayerNick(playerId);
        respawnPlayer(characterId, playerNick, playerId);
    }

    @Override
    public void start() {

    }

    private void respawnPlayer(CharacterId characterId, String nick, PlayerId playerId) {
        PlayerCharacter newPlayerCharacter = new PlayerCharacter(characterId, nick, playerId);
        Move initialMove = positionService.getInitialMove(characterId);
        gameEventScheduler.schedule(singletonList(new CharacterSpawned(newPlayerCharacter, initialMove)), SPAWN_TIME);
    }
}
