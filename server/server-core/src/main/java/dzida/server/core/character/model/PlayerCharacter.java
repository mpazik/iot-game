package dzida.server.core.character.model;

import dzida.server.core.CharacterId;
import dzida.server.core.PlayerId;

public class PlayerCharacter implements Character {
    private final CharacterId id;
    private final String nick;
    private final int type;
    private final PlayerId playerId;

    public PlayerCharacter(CharacterId id, String nick, PlayerId playerId) {
        this.id = id;
        this.nick = nick;
        this.playerId = playerId;
        type = Character.Player;
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

    public PlayerId getPlayerId() {
        return playerId;
    }
}
