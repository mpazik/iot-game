package dzida.server.core.character.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.CharacterEvent;
import org.jetbrains.annotations.NotNull;

public class CharacterDied implements CharacterEvent {
    public final Id<Character> characterId;

    public CharacterDied(Id<Character> characterId) {
        this.characterId = characterId;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return characterId;
    }
}
