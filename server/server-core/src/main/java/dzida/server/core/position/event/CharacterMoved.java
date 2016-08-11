package dzida.server.core.position.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.basic.unit.Move;

public class CharacterMoved implements GameEvent {
    public final Id<Character> characterId;
    public final Move move;

    public CharacterMoved(Id<Character> characterId, Move move) {
        this.characterId = characterId;
        this.move = move;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterMoved;
    }
}
