package dzida.server.app;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import dzida.server.core.skill.Skill;
import lombok.Value;

import java.util.Map;

public class SkillStore {
    private final StaticDataLoader staticDataLoader = new StaticDataLoader();

    Map<Integer, Skill> loadSkills() {

        Map<Integer, SkillBean> skillBeans = staticDataLoader.loadJsonFromServer("skills.json", new TypeToken<Map<Integer, SkillBean>>() {
        });

        return Maps.transformValues(skillBeans, bean -> new Skill(
                bean.getId(),
                bean.getType(),
                bean.getDamage(),
                bean.getRange(),
                bean.getCooldown(),
                bean.getTarget())
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
