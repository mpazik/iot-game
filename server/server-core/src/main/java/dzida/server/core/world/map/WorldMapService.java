package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;

public class WorldMapService {

    public static final String ServiceKey = "world";

    private final WorldMap worldMap;

    private WorldMapService(WorldMapStore worldMapStore, Key<WorldMap> worldMapKey) {
        this.worldMap = worldMapStore.getMap(worldMapKey);
    }

    static public WorldMapService create(WorldMapStore worldMapStore, Key<WorldMap> worldMapKey) {
        return new WorldMapService(worldMapStore, worldMapKey);
    }

    public WorldMap getState() {
        return worldMap;
    }

    public String getKey() {
        return ServiceKey;
    }
}
