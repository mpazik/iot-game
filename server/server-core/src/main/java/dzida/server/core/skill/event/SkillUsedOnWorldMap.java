package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;
import lombok.Value;

@Value
public class SkillUsedOnWorldMap implements GameEvent {
    CharacterId casterId;
    Id<Skill> skillId;
    double x;
    double y;

    @Override
    public int getId() {
        return GameEvent.SkillUsedOnWorldMap;
    }
}
