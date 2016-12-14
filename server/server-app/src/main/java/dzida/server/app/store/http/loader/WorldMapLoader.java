package dzida.server.app.store.http.loader;

import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.world.map.Tileset;
import dzida.server.app.instance.world.map.WorldMap;
import dzida.server.app.world.map.TilesetData;
import dzida.server.app.world.map.WorldMapData;

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
