package dzida.server.app.instance;

import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public class PlayerCharacter implements Character {
    private final Id<Character> id;
    private final Id<User> userId;
    private final int type;

    public PlayerCharacter(Id<Character> id, Id<User> userId) {
        this.id = id;
        this.userId = userId;
        type = Type.Player;
    }

    public Id<Character> getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public Id<User> getUserId() {
        return userId;
    }
}
