package dzida.server.app.store.memory;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillStore;

import java.util.Map;

public class SkillStoreInMemory implements SkillStore {
    private final Map<Id<Skill>, Skill> skills;

    public SkillStoreInMemory(Map<Id<Skill>, Skill> skills) {
        this.skills = skills;
    }

    @Override
    public Skill getSkill(Id<Skill> skillId) {
        return skills.get(skillId);
    }
}
