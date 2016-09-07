package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.CharacterEvent;
import org.jetbrains.annotations.NotNull;

public final class CharacterHealed implements CharacterEvent {
    public final Id<Character> characterId;
    public final double healed;

    public CharacterHealed(Id<Character> characterId, double healed) {
        this.characterId = characterId;
        this.healed = healed;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return characterId;
    }
}
