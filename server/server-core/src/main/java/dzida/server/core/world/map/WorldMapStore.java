package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.object.WorldObject;

import java.util.List;

public interface WorldMapStore {
    WorldMap getMap(Key<WorldMap> worldMapKey);

    List<WorldObject> getInitialMapObjects(Key<WorldMap> worldMapKey);

    Tileset getTileset(Key<Tileset> tilesetKey);
}
