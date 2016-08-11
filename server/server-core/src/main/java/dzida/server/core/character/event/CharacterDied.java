package dzida.server.core.character.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.event.GameEvent;
import dzida.server.core.character.model.Character;

public class CharacterDied implements GameEvent {
    public final Id<Character> characterId;

    public CharacterDied(Id<Character> characterId) {
        this.characterId = characterId;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterDied;
    }
}
