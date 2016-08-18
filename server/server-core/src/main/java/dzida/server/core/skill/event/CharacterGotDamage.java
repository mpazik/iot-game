package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;

public class CharacterGotDamage implements GameEvent {
    public final Id<Character> characterId;
    public final double damage;

    public CharacterGotDamage(Id<Character> characterId, double damage) {
        this.characterId = characterId;
        this.damage = damage;
    }
}
