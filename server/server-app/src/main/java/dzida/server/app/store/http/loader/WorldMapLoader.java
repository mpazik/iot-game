package dzida.server.app.store.http.loader;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.map.Tileset;
import dzida.server.core.world.map.WorldMap;

public class WorldMapLoader {
    private final StaticDataLoader staticDataLoader;

    public WorldMapLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public WorldMap loadMap(Key<WorldMap> worldMapKey) {
        return staticDataLoader.loadJsonFromServer("maps/" + worldMapKey.getValue() + ".json", WorldMap.class);
    }

    public Tileset loadTileset(Key<Tileset> tilesetKey) {
        return staticDataLoader.loadJsonFromServer("tilesets/" + tilesetKey.getValue() + ".json", Tileset.class);
    }
}
