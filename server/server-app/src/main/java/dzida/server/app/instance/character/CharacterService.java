package dzida.server.app.instance.character;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;

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
