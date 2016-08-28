package dzida.server.app.leaderboard;

import com.google.common.collect.Iterables;
import dzida.server.app.instance.scenario.ScenarioEventBox;
import dzida.server.app.instance.scenario.ScenarioStore;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioFinished;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioStarted;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.user.User;
import dzida.server.app.user.UserService;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.scenario.ScenarioEnd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Leaderboard {
    private final UserService userService;
    private final ScenarioStore scenarioStore;
    private final Ranking ranking;

    public Leaderboard(UserService userService, ScenarioStore scenarioStore) {
        this.userService = userService;
        this.scenarioStore = scenarioStore;
        ranking = new Ranking();
    }

    public void update() {
        List<ScenarioEventBox> events = scenarioStore.getEvents(ranking.timestamp);
        if (events.size() == 0) return;

        events.forEach(eventBox -> {
            if (eventBox.event instanceof ScenarioStarted) {
                ScenarioStarted scenarioStarted = (ScenarioStarted) eventBox.event;
                if (scenarioStarted.scenario instanceof Survival) {
                    Survival survival = (Survival) scenarioStarted.scenario;
                    ranking.scenarioStarter(eventBox.scenarioId, survival);
                }
            } else if (eventBox.event instanceof ScenarioFinished) {
                ScenarioFinished scenarioEnd = (ScenarioFinished) eventBox.event;
                ranking.scenarioEnded(eventBox.scenarioId, scenarioEnd.resolution);
            }
        });
        ranking.timestamp = Iterables.getLast(events).createdAt + 1;
    }

    public List<PlayerScore> getListOfSurvivalRecords() {
        return ranking.getListOfSurvivalRecords();
    }

    public PlayerScore getPlayerScore(Id<User> userId) {
        return ranking.getPlayerScore(userId);
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

    private final class Ranking {
        private final Map<Id<User>, Integer> highScores = new HashMap<>();
        private final Map<Id<Scenario>, Survival> scenariosRunning = new HashMap<>();
        private long timestamp = 0;

        public void scenarioStarter(Id<Scenario> scenarioId, Survival survival) {
            scenariosRunning.put(scenarioId, survival);
        }

        public void scenarioEnded(Id<Scenario> scenarioId, ScenarioEnd.Resolution resolution) {
            if (!scenariosRunning.containsKey(scenarioId)) return;
            Survival scenario = scenariosRunning.remove(scenarioId);

            if (resolution != ScenarioEnd.Resolution.Victory) return;

            int difficultyLevel = scenario.getDifficultyLevel();
            scenario.getAttendees().forEach(userId -> {
                Integer highScore = highScores.get(userId);
                if (highScore == null || difficultyLevel > highScore) {
                    highScores.put(userId, difficultyLevel);
                }
            });
        }

        public List<PlayerScore> getListOfSurvivalRecords() {
            int limit = 10;
            List<Id<User>> topUsers = getTopUsers();

            int numOfRecords = Math.min(limit, topUsers.size());
            return IntStream.range(0, numOfRecords).mapToObj(index -> {
                Id<User> userId = topUsers.get(index);
                String userNick = userService.getUserNick(userId);
                return new PlayerScore(userNick, highScores.get(userId), getPositionFromIndex(index));
            }).collect(Collectors.toList());
        }

        public PlayerScore getPlayerScore(Id<User> userId) {
            List<Id<User>> topUsers = getTopUsers();
            int userPosition = getPositionFromIndex(topUsers.indexOf(userId));
            String userNick = userService.getUserNick(userId);
            return new PlayerScore(userNick, highScores.get(userId), userPosition);
        }

        private List<Id<User>> getTopUsers() {
            return highScores.entrySet().stream()
                    .sorted((o1, o2) -> o1.getValue() - o2.getValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        private int getPositionFromIndex(int index) {
            return index + 1;
        }
    }
}
