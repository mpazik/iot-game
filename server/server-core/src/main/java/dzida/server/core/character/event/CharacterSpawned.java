package dzida.server.core.character.event;

import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.model.Move;
import lombok.Value;

@Value
public class CharacterSpawned implements GameEvent {
    Character character;
    Move move;

    @Override
    public int getId() {
        return GameEvent.CharacterSpawned;
    }
}
