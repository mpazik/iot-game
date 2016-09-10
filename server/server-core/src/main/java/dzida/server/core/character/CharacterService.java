package dzida.server.core.character;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

import java.util.List;

public interface CharacterService {
    static CharacterService create() {
        return new CharacterServiceImpl();
    }

    List<Character> getState();

    String getKey();

    boolean isCharacterEnemyFor(Id<Character> character1, Id<Character> character2);

    void processEvent(GameEvent gameEvent);

    <T extends Character> List<T> getCharactersOfType(Class<T> clazz);

    boolean isCharacterLive(Id<Character> characterId);

    Character getCharacter(Id<Character> characterId);
}
