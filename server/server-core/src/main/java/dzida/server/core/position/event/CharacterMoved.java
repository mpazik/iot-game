package dzida.server.core.position.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.basic.unit.Move;
import lombok.Value;

@Value
public class CharacterMoved implements GameEvent {
    CharacterId characterId;
    Move move;

    @Override
    public int getId() {
        return GameEvent.CharacterMoved;
    }
}
