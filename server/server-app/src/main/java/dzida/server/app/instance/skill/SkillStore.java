package dzida.server.app.instance.skill;

import dzida.server.app.basic.entity.Id;

public interface SkillStore {
    Skill getSkill(Id<Skill> skillId);
}
