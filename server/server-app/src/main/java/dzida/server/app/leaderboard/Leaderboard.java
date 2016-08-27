package dzida.server.app.leaderboard;

import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    private final Map<Id<User>, Integer> highestScores;

    public Leaderboard() {
        highestScores = new HashMap<>();
    }

    public void notePlayerScore(Id<User> userId, Integer score) {
        if (highestScores.containsKey(userId)) {
            if (highestScores.get(userId) < score) {
                highestScores.put(userId, score);
            }
        } else {
            highestScores.put(userId, score);
        }
    }

    public List<PlayerScore> getListOfSurvivalRecords() {
        int limit = 10;
        List<Id<User>> topUsers = getTopUsers();

        int numOfRecords = Math.min(limit, topUsers.size());
        return IntStream.range(0, numOfRecords).mapToObj(index -> {
            Id<User> userid = topUsers.get(index);
            return new PlayerScore("", highestScores.get(userid), getPositionFromIndex(index));
        }).collect(Collectors.toList());
    }

    public PlayerScore getPlayerScore(Id<User> userId) {
        List<Id<User>> topUsers = getTopUsers();
        int userPosition = getPositionFromIndex(topUsers.indexOf(userId));
        return new PlayerScore("", highestScores.get(userId), userPosition);
    }


    private List<Id<User>> getTopUsers() {
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
