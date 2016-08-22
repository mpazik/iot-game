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

        public Data(String nick) {
            this.nick = nick;
        }

        public String getNick() {
            return nick;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return Objects.equals(nick, data.nick);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nick);
        }
    }
}
