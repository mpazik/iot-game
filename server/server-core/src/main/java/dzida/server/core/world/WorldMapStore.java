package dzida.server.core.world;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.model.Tileset;
import dzida.server.core.world.model.WorldMap;

public interface WorldMapStore {
    WorldMap getMap(Key<WorldMap> worldMapKey);

    Tileset getTileset(Key<Tileset> tilesetKey);
}
