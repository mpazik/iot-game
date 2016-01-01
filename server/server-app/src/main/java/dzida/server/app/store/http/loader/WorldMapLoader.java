package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.model.WorldMap;

public class WorldMapLoader {
    private final StaticDataLoader staticDataLoader;

    public WorldMapLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public WorldMap loadMap(Key<WorldMap> name) {
        return staticDataLoader.loadJsonFromServer("maps/" + name.getValue() + ".json", TypeToken.of(WorldMap.class));
    }
}
