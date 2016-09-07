package dzida.server.core.position.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.CharacterEvent;
import org.jetbrains.annotations.NotNull;

public class CharacterMoved implements CharacterEvent {
    public final Id<Character> characterId;
    public final Move move;

    public CharacterMoved(Id<Character> characterId, Move move) {
        this.characterId = characterId;
        this.move = move;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return characterId;
    }
}
