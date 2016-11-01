package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import dzida.server.core.world.object.WorldObjectKind;

import java.util.List;

public class WorldObjectKindLoader {
    private final StaticDataLoader staticDataLoader;

    public WorldObjectKindLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public List<WorldObjectKind> loadWorldObjectKinds() {
        return staticDataLoader.loadJsonFromServer("entities/objects.json", new TypeToken<List<WorldObjectKind>>() {
        });
    }
}
