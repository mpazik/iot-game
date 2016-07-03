package dzida.server.core.character.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;

public class CharacterDied implements GameEvent {
    public final CharacterId characterId;

    public CharacterDied(CharacterId characterId) {
        this.characterId = characterId;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterDied;
    }
}
