package dzida.server.core.character.event;

import dzida.server.core.basic.unit.Move;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.SkillService.SkillData;
import lombok.Value;

@Value
public class CharacterSpawned implements GameEvent {
    Character character;
    Move move;
    SkillData skillData;

    @Override
    public int getId() {
        return GameEvent.CharacterSpawned;
    }
}
