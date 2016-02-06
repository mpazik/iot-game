package dzida.server.app;

import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerStore;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    private final PlayerStore playerStore;

    public Leaderboard(PlayerStore playerStore) {
        this.playerStore = playerStore;
    }

    public List<PlayerScore> getListOfSurvivalRecords() {
        int limit = 10;
        List<Player.Entity> topPlayerData = getTopPlayerData();

        int numOfRecords = Math.min(limit, topPlayerData.size());
        return IntStream.range(0, numOfRecords).mapToObj(index -> {
            Player.Data data = topPlayerData.get(index).getData();
            return new PlayerScore(data.getNick(), data.getHighestDifficultyLevel(), getPositionFromIndex(index));
        }).collect(Collectors.toList());
    }

    public PlayerScore getPlayerScore(Player.Id playerId) {
        Player.Entity player = playerStore.getPlayer(playerId);
        List<Player.Entity> topPlayerData = getTopPlayerData();
        int playerPosition = getPositionFromIndex(topPlayerData.indexOf(player));
        return new PlayerScore(player.getData().getNick(), player.getData().getHighestDifficultyLevel(), playerPosition);
    }


    private List<Player.Entity> getTopPlayerData() {
        return playerStore.getAllPlayers()
                .filter(player -> player.getData().getHighestDifficultyLevel() > 0)
                .collect(Collectors.toList());
    }

    private int getPositionFromIndex(int index) {
        return index + 1;
    }

    @Value
    public static final class PlayerScore {
        String nick;
        int record;
        int position;
    }
}
