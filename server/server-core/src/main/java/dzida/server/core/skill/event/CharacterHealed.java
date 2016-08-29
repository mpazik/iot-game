package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

public final class CharacterHealed implements GameEvent {
    public final Id<Character> characterId;
    public final double healed;

    public CharacterHealed(Id<Character> characterId, double healed) {
        this.characterId = characterId;
        this.healed = healed;
    }
}
