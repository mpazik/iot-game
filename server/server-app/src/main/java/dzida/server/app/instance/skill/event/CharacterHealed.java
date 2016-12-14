package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
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
