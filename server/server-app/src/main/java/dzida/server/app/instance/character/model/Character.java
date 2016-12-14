package dzida.server.app.instance.character.model;

import dzida.server.app.basic.entity.Id;

public interface Character {
    Id<Character> getId();

    int getType();

    interface Type {
        int Player = 0;
        int Bot = 1;
    }
}