package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.skill.Skill;

import java.util.Map;

public class SkillLoader {
    private final StaticDataLoader staticDataLoader;

    public SkillLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public Map<Id<Skill>, Skill> loadSkills() {
        return staticDataLoader.loadJsonFromServer("skills.json", new TypeToken<Map<Id<Skill>, Skill>>() {
        });
    }
}
