package dzida.server.app;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    private final Map<Id<Player>, Integer> highestScores;

    public Leaderboard() {
        highestScores = new HashMap<>();
    }

    public void notePlayerScore(Id<Player> playerId, Integer score) {
        if (highestScores.containsKey(playerId)) {
            if (highestScores.get(playerId) < score) {
                highestScores.put(playerId, score);
            }
        } else {
            highestScores.put(playerId, score);
        }
    }

    public List<PlayerScore> getListOfSurvivalRecords() {
        int limit = 10;
        List<Id<Player>> topPlayerData = getTopPlayerData();

        int numOfRecords = Math.min(limit, topPlayerData.size());
        return IntStream.range(0, numOfRecords).mapToObj(index -> {
            Id<Player> playerId = topPlayerData.get(index);
            return new PlayerScore("", highestScores.get(playerId), getPositionFromIndex(index));
        }).collect(Collectors.toList());
    }

    public PlayerScore getPlayerScore(Id<Player> playerId) {
        List<Id<Player>> topPlayerData = getTopPlayerData();
        int playerPosition = getPositionFromIndex(topPlayerData.indexOf(playerId));
        return new PlayerScore("", highestScores.get(playerId), playerPosition);
    }


    private List<Id<Player>> getTopPlayerData() {
        return highestScores.entrySet().stream()
                .sorted((o1, o2) -> o1.getValue() - o2.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int getPositionFromIndex(int index) {
        return index + 1;
    }

    public static final class PlayerScore {
        String nick;
        int record;
        int position;

        public PlayerScore(String nick, int record, int position) {
            this.nick = nick;
            this.record = record;
            this.position = position;
        }
    }
}
