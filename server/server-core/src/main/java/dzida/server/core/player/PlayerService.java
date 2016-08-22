package dzida.server.core.player;

import dzida.server.core.basic.Error;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.entity.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerService {
    private final PlayerStore playerStore;
    private final Map<Id<Player>, Player> playingPlayers = new HashMap<>();

    public PlayerService(PlayerStore playerStore) {
        this.playerStore = playerStore;
    }


    public Player getPlayer(Id<Player> playerId) {
        return playingPlayers.get(playerId);
    }

    public Optional<Id<Player>> findPlayer(String nick) {
        return playerStore.findPlayerByNick(nick);
    }

    public void loginPlayer(Id<Player> playerId) {
        Player player = playerStore.getPlayer(playerId);
        playingPlayers.put(playerId, player);

    }

    public Outcome<Player> createPlayer(String nick) {
        Optional<Id<Player>> player = findPlayer(nick);
        if (player.isPresent()) {
            return Outcome.error(new Error("player already exists"));
        }
        Player.Data playerData = new Player.Data(nick);

        Player playerEntity = playerStore.createPlayer(playerData);
        return Outcome.ok(playerEntity);
    }

    public void logoutPlayer(Id<Player> playerId) {
        playingPlayers.remove(playerId);
    }

    public void updatePlayerData(Id<Player> playerId, Player.Data playerData) {
        playerStore.updatePlayer(playerId, playerData);
    }
}
