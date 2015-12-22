package dzida.server.core.skill.event;

import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class CharacterGotDamage implements GameEvent {
    CharacterId characterId;
    double damage;

    @Override
    public int getId() {
        return GameEvent.CharacterGotDamage;
    }
}
