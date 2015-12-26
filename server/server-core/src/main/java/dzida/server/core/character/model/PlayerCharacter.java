package dzida.server.core.character.model;

import dzida.server.core.character.CharacterId;
import dzida.server.core.player.Player;

public class PlayerCharacter implements Character {
    private final CharacterId id;
    private final String nick;
    private final int type;
    private final Player.Id playerId;

    public PlayerCharacter(CharacterId id, String nick, Player.Id playerId) {
        this.id = id;
        this.nick = nick;
        this.playerId = playerId;
        type = Character.Type.Player;
    }

    public CharacterId getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getNick() {
        return nick;
    }

    public Player.Id getPlayerId() {
        return playerId;
    }
}
