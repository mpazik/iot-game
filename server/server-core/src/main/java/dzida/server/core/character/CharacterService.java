package dzida.server.core.character;

import dzida.server.core.CharacterId;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface CharacterService {
    static CharacterService create() {
        return new CharacterServiceImpl();
    }

    List<Character> getState();

    String getKey();

    boolean isCharacterEnemyFor(CharacterId character1, CharacterId character2);

    void processEvent(GameEvent gameEvent);

    Stream<Character> getCharactersOfType(int type);

    int getCharacterType(CharacterId characterId);

    Optional<PlayerCharacter> getPlayerCharacter(CharacterId characterId);

    boolean isCharacterLive(CharacterId characterId);
}
