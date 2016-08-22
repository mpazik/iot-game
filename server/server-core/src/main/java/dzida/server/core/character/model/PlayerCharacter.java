package dzida.server.core.character.model;

import dzida.server.core.basic.entity.Id;

public class PlayerCharacter implements Character {
    private final Id<Character> id;
    private final String nick;
    private final int type;

    public PlayerCharacter(Id<Character> id, String nick) {
        this.id = id;
        this.nick = nick;
        type = Character.Type.Player;
    }

    public Id<Character> getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getNick() {
        return nick;
    }
}
