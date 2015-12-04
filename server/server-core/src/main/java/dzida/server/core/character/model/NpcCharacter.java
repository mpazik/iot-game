package dzida.server.core.character.model;

import dzida.server.core.CharacterId;

public class NpcCharacter implements Character {
    private final CharacterId id;
    private final int type;
    private final int botType;

    public NpcCharacter(CharacterId id, int botType) {
        this.id = id;
        this.type = Character.Bot;
        this.botType = botType;
    }

    public CharacterId getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getBotType() {
        return botType;
    }
}
