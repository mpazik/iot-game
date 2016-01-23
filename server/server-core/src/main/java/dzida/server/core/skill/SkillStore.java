package dzida.server.core.skill;

import dzida.server.core.basic.entity.Id;

public interface SkillStore {
    Skill getSkill(Id<Skill> skillId);
}
