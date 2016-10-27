package dzida.server.app.store.http.loader;

import dzida.server.app.world.map.TilesetData;
import dzida.server.app.world.map.WorldMapData;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.map.Tileset;
import dzida.server.core.world.map.WorldMap;

public class WorldMapLoader {
    private final StaticDataLoader staticDataLoader;

    public WorldMapLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public WorldMapData loadMap(Key<WorldMap> worldMapKey) {
        return staticDataLoader.loadJsonFromServer("maps/" + worldMapKey.getValue() + ".json", WorldMapData.class);
    }

    public TilesetData loadTileset(Key<Tileset> tilesetKey) {
        return staticDataLoader.loadJsonFromServer("tilesets/" + tilesetKey.getValue() + ".json", TilesetData.class);
    }
}
