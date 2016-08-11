package dzida.server.app.scenario;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.player.PlayerWillRespawn;

import java.time.Instant;

import static java.util.Collections.singletonList;

public class OpenWorldScenarioLogic implements ScenarioLogic {
    private static final int SPAWN_TIME = 4000;

    private final PlayerService playerService;
    private final GameEventScheduler gameEventScheduler;
    private final CharacterCommandHandler characterCommandHandler;

    public OpenWorldScenarioLogic(
            PlayerService playerService,
            GameEventScheduler gameEventScheduler,
            CharacterCommandHandler characterCommandHandler) {
        this.playerService = playerService;
        this.gameEventScheduler = gameEventScheduler;
        this.characterCommandHandler = characterCommandHandler;
    }

    @Override
    public void handlePlayerDead(CharacterId characterId, Id<Player> playerId) {
        String playerNick = playerService.getPlayer(playerId).getData().getNick();
        respawnPlayer(characterId, playerNick, playerId);
    }

    @Override
    public void start() {

    }

    private void respawnPlayer(CharacterId characterId, String nick, Id<Player> playerId) {
        PlayerCharacter newPlayerCharacter = new PlayerCharacter(characterId, nick, playerId);
        long respawnTime = Instant.now().plusMillis(SPAWN_TIME).toEpochMilli();
        gameEventScheduler.dispatch(singletonList(new PlayerWillRespawn(playerId, respawnTime)));
        gameEventScheduler.schedule(characterCommandHandler.spawnCharacter(newPlayerCharacter), SPAWN_TIME);
    }
}
