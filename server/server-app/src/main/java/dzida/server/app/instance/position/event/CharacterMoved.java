package dzida.server.app.instance.position.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
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
