package dzida.server.core.character.model;

import dzida.server.core.basic.entity.Id;

public interface Character {
    interface Type {
        int Player = 0;
        int Bot = 1;
    }

    Id<Character> getId();

    int getType();
}