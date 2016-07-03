package dzida.server.core.player;

import dzida.server.core.basic.Error;
import dzida.server.core.basic.Outcome;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerService {
    private final PlayerStore playerStore;
    private final Map<Player.Id, Player.Entity> playingPlayers = new HashMap<>();

    public PlayerService(PlayerStore playerStore) {
        this.playerStore = playerStore;
    }


    public Player.Entity getPlayer(Player.Id playerId) {
        return playingPlayers.get(playerId);
    }

    public Optional<Player.Id> findPlayer(String nick) {
        return playerStore.findPlayerByNick(nick);
    }

    public void loginPlayer(Player.Id playerId) {
        Player.Entity player = playerStore.getPlayer(playerId);
        playingPlayers.put(playerId, player);

    }

    public Outcome<Player.Entity> createPlayer(String nick) {
        Optional<Player.Id> player = findPlayer(nick);
        if (player.isPresent()) {
            return Outcome.<Player.Entity>error(new Error("player already exists"));
        }
        Player.Data playerData = new Player.Data(nick, 0, 1);

        Player.Entity playerEntity = playerStore.createPlayer(playerData);
        return Outcome.ok(playerEntity);
    }

    public boolean isPlayerPlaying(Player.Id playerId) {
        return playingPlayers.containsKey(playerId);
    }

    public void logoutPlayer(Player.Id playerId) {
        playingPlayers.remove(playerId);
    }

    public void updatePlayerData(Player.Id playerId, Player.Data playerData) {
        playerStore.updatePlayer(playerId, playerData);
    }
}
