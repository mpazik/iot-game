package dzida.server.core.world;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.model.WorldMap;

public class WorldService {

    public static final String ServiceKey = "world";

    private final WorldMap worldMap;

    private WorldService(WorldMapStore worldMapStore, Key<WorldMap> worldMapKey) {
        this.worldMap = worldMapStore.getMap(worldMapKey);
    }

    static public WorldService create(WorldMapStore worldMapStore, Key<WorldMap> worldMapKey) {
        return new WorldService(worldMapStore, worldMapKey);
    }

    public WorldMap getState() {
        return worldMap;
    }

    public String getKey() {
        return ServiceKey;
    }
}
