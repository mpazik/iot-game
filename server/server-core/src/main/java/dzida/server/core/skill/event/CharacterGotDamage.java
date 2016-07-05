package dzida.server.core.skill.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;

public class CharacterGotDamage implements GameEvent {
    public final CharacterId characterId;
    public final double damage;

    public CharacterGotDamage(CharacterId characterId, double damage) {
        this.characterId = characterId;
        this.damage = damage;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterGotDamage;
    }
}
