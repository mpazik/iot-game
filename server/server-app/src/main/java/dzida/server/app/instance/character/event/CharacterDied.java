package dzida.server.app.instance.character.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
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
