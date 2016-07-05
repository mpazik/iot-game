package dzida.server.core.position.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.basic.unit.Move;

public class CharacterMoved implements GameEvent {
    public final CharacterId characterId;
    public final Move move;

    public CharacterMoved(CharacterId characterId, Move move) {
        this.characterId = characterId;
        this.move = move;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterMoved;
    }
}
