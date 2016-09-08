package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import dzida.server.app.achievement.Achievement;

import java.util.List;

public class AchievementLoader {
    private final StaticDataLoader staticDataLoader;

    public AchievementLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public List<Achievement> loadAchievements() {
        return staticDataLoader.loadJsonFromServer("achievements.json", new TypeToken<List<Achievement>>() {
        });
    }
}
