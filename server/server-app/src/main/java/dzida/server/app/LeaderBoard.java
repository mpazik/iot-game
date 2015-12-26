package dzida.server.app;

import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerStore;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

public class Leaderboard {
    private final PlayerStore playerStore;

    public Leaderboard(PlayerStore playerStore) {
        this.playerStore = playerStore;
    }

    public List<Record> listOfSurvivalRecords() {
        return playerStore.getAllPlayers()
                .map(Player.Entity::getData)
                .filter(data -> data.getHighestDifficultyLevel() > 0)
                .sorted((o1, o2) -> o2.getHighestDifficultyLevel() - o1.getHighestDifficultyLevel())
                .limit(10)
                .map(data -> new Record(data.getNick(), data.getHighestDifficultyLevel()))
                .collect(Collectors.toList());
    }

    @Value
    public static final class Record {
        String nick;
        int record;
    }
}
