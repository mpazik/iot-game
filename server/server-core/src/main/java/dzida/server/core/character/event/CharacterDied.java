package dzida.server.core.character.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class CharacterDied implements GameEvent {
    CharacterId characterId;

    @Override
    public int getId() {
        return GameEvent.CharacterDied;
    }
}
