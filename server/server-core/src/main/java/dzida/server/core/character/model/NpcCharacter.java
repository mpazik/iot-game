package dzida.server.core.character.model;

import dzida.server.core.basic.entity.Id;

public class NpcCharacter implements Character {
    private final Id<Character> id;
    private final int type;
    private final int botType;

    public NpcCharacter(Id<Character> id, int botType) {
        this.id = id;
        this.type = Character.Type.Bot;
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
