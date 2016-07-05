package dzida.server.core.player;

import java.util.Objects;

public class Player {

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


    public static final class Id extends dzida.server.core.basic.entity.Id<Player.Data> {
        // to do make it private
        public Id(long id) {
            super(id);
        }
    }

    public static final class Entity extends dzida.server.core.basic.entity.Entity<Player.Id, Player.Data> {

        public Entity(Id id, Player.Data data) {
            super(id, data);
        }
    }
}
