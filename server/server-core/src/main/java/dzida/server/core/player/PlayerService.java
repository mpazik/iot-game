package dzida.server.core.player;

import java.util.*;

public class PlayerService {

    private Map<PlayerId, PlayerData> playingPlayers = new HashMap<>();
    private Map<String, PlayerData> persistedData = new HashMap<>();

    public PlayerData getPlayerData(PlayerId playerId) {
        return playingPlayers.get(playerId);
    }

    public Optional<PlayerId> loadPlayer(String nick) {
        // this method should check player from data base.
        PlayerId playerId = generatePlayerId();
        PlayerData playerData = readPlayerData(nick);
        playingPlayers.put(playerId, playerData);
        persistedData.put(nick, playerData);
        return Optional.of(playerId);
    }

    public boolean isPlayerPlaying(PlayerId playerId) {
        return playingPlayers.containsKey(playerId);
    }

    public void logoutPlayer(PlayerId playerId) {
        playingPlayers.remove(playerId);
        playingPlayers.remove(playerId);
    }

    private PlayerData readPlayerData(String nick) {
        if (persistedData.containsKey(nick)) {
            return persistedData.get(nick);
        }
        return new PlayerData(nick, 0, 1);
    }

    private PlayerId generatePlayerId() {
        return new PlayerId((int) Math.round((Math.random() * 100000)));
    }

    public void updatePlayerData(PlayerId playerId, PlayerData playerData) {
        playingPlayers.put(playerId, playerData);
    }

    public boolean isPlayerPlaying(String nick) {
        return playingPlayers.values().stream().anyMatch(playerData -> playerData.getNick().equals(nick));
    }
}
