package dzida.server.core.player;

import lombok.Value;

import java.util.*;

public class PlayerService {

    private Map<PlayerId, PlayerData> players = new HashMap<>();
    private Set<PlayerId> playingPlayers = new HashSet<>();

    public String getPlayerNick(PlayerId playerId) {
        return players.get(playerId).getNick();
    }

    public Optional<PlayerId> loadPlayer(String nick) {
        // this method should check player from data base.
        PlayerId playerId = generatePlayerId();
        players.put(playerId, new PlayerData(nick));
        playingPlayers.add(playerId);
        return Optional.of(playerId);
    }

    public boolean isPlayerPlaying(PlayerId playerId) {
        return playingPlayers.contains(playerId);
    }

    public void logoutPlayer(PlayerId playerId) {
        playingPlayers.remove(playerId);
    }

    private PlayerId generatePlayerId() {
        return new PlayerId((int) Math.round((Math.random() * 100000)));
    }

    @Value
    private static class PlayerData {
        String nick;
    }
}
