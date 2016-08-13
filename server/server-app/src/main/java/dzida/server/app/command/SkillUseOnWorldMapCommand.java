package dzida.server.app.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.skill.Skill;

public class SkillUseOnWorldMapCommand implements InstanceCommand {
    public final Id<Skill> skillId;
    public final double x;
    public final double y;

    private SkillUseOnWorldMapCommand(Id<Skill> skillId, double x, double y) {
        this.skillId = skillId;
        this.x = x;
        this.y = y;
    }
}
