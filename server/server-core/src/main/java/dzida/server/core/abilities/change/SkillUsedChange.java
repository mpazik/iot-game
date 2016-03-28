package dzida.server.core.abilities.change;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.entity.Change;
import dzida.server.core.abilities.Abilities;
import dzida.server.core.skill.Skill;

import java.time.Instant;

public class SkillUsedChange implements Change<Abilities> {
    public final Id<Skill> usedSkillId;
    public final Instant when;

    public SkillUsedChange(Id<Skill> skillId, Instant when) {
        this.usedSkillId = skillId;
        this.when = when;
    }
}
