package dzida.server.core.abilities.command;

import dzida.server.core.abilities.Abilities;
import dzida.server.core.skill.Skill;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.entity.Command;
import dzida.server.core.entity.EntityId;

public class CastSkillCommand implements Command {
    public final EntityId<Abilities> casterId;
    public final Id<Skill> skillId;

    public CastSkillCommand(EntityId<Abilities> casterId, Id<Skill> skillId) {
        this.casterId = casterId;
        this.skillId = skillId;
    }
}
