package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;
import dzida.server.core.world.object.WorldObject;
import lombok.Value;

@Value
public class SkillUsedOnWorldObject implements GameEvent {
    CharacterId casterId;
    Id<Skill> skillId;
    Id<WorldObject> worldObjectId;

    @Override
    public int getId() {
        return GameEvent.SkillUsedOnWorldObject;
    }
}
