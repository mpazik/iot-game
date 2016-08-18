package dzida.server.core.character.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

public class CharacterDied implements GameEvent {
    public final Id<Character> characterId;

    public CharacterDied(Id<Character> characterId) {
        this.characterId = characterId;
    }
}
