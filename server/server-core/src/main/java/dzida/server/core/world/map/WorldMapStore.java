package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;

public interface WorldMapStore {
    WorldMap getMap(Key<WorldMap> worldMapKey);

    Tileset getTileset(Key<Tileset> tilesetKey);
}
