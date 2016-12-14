package dzida.server.app.instance.world.map;

import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.world.object.WorldObject;

import java.util.List;

public interface WorldMapStore {
    WorldMap getMap(Key<WorldMap> worldMapKey);

    List<WorldObject> getInitialMapObjects(Key<WorldMap> worldMapKey);

    Tileset getTileset(Key<Tileset> tilesetKey);
}
