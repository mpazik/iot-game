package dzida.server.app.store.http.loader;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.skill.Skill;
import lombok.Value;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SkillLoader {
    private final StaticDataLoader staticDataLoader;

    public SkillLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public Map<Id<Skill>, Skill> loadSkills() {
        Set<Skill> skills = staticDataLoader.loadJsonFromServer("skills.json", new TypeToken<Map<Integer, SkillBean>>() {
        }).values().stream().map(this::createSkill).collect(Collectors.toSet());
        return Maps.uniqueIndex(skills, Skill::getId);
    }

    private Skill createSkill(SkillBean skillBean) {
        final long id = (long) skillBean.getId();
        return new Skill(
                new Id<>(id),
                skillBean.getType(),
                skillBean.getDamage(),
                skillBean.getRange(),
                skillBean.getCooldown(),
                skillBean.getTarget()
        );
    }

    @Value
    private static class SkillBean {
        int id;
        int type;
        double damage;
        double range;
        int cooldown;
        int target;
    }
}
