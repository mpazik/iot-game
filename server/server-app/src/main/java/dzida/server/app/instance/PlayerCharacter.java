package dzida.server.app.instance;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.user.User;

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
