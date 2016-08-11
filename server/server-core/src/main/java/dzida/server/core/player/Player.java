package dzida.server.core.player;

import dzida.server.core.basic.entity.Id;

import java.util.Objects;

public class Player {
    private final Id<Player> id;
    private final Data data;

    public Player(Id<Player> id, Player.Data data) {
        this.id = id;
        this.data = data;
    }

    public Id<Player> getId() {
        return id;
    }

    public Data getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id) &&
                Objects.equals(data, player.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }

    public static final class Data extends dzida.server.core.basic.entity.Data {
        private final String nick;
        private final int highestDifficultyLevel;
        private final int lastDifficultyLevel;

        public Data(String nick, int highestDifficultyLevel, int lastDifficultyLevel) {
            this.nick = nick;
            this.highestDifficultyLevel = highestDifficultyLevel;
            this.lastDifficultyLevel = lastDifficultyLevel;
        }

        public String getNick() {
            return nick;
        }

        public int getHighestDifficultyLevel() {
            return highestDifficultyLevel;
        }

        public int getLastDifficultyLevel() {
            return lastDifficultyLevel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return highestDifficultyLevel == data.highestDifficultyLevel &&
                    lastDifficultyLevel == data.lastDifficultyLevel &&
                    Objects.equals(nick, data.nick);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nick, highestDifficultyLevel, lastDifficultyLevel);
        }
    }
}
