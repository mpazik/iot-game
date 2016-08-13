package dzida.server.app.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.skill.Skill;
import dzida.server.core.world.object.WorldObject;

public class SkillUseOnWorldObjectCommand implements InstanceCommand {
    public final Id<Skill> skillId;
    public final Id<WorldObject> target;

    SkillUseOnWorldObjectCommand(Id<Skill> skillId, Id<WorldObject> target) {
        this.skillId = skillId;
        this.target = target;
    }
}
