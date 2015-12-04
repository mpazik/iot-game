package dzida.server.core.character.model;

import dzida.server.core.CharacterId;

public interface Character {
    int Player = 0;
    int Bot = 1;

    CharacterId getId();

    int getType();
}