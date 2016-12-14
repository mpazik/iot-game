package dzida.server.app.store.memory;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.skill.SkillStore;

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
