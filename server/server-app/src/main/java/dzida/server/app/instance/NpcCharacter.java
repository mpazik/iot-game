package dzida.server.app.instance;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;

public class NpcCharacter implements Character {
    private final Id<Character> id;
    private final int type;
    private final int botType;

    public NpcCharacter(Id<Character> id, int botType) {
        this.id = id;
        this.type = Type.Bot;
        this.botType = botType;
    }

    public Id<Character> getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getBotType() {
        return botType;
    }
}
