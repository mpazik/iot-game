package dzida.server.core.world.object;

import java.util.List;

public class WorldObjectService {
    public static final String Key = "worldObject";
    private final WorldObjectStore worldObjectStore;

    public WorldObjectService(WorldObjectStore worldObjectStore) {
        this.worldObjectStore = worldObjectStore;
    }

    static public WorldObjectService create(WorldObjectStore worldObjectStore) {
        return new WorldObjectService(worldObjectStore);
    }

    public List<WorldObject.Entity> getState() {
        return worldObjectStore.getAll();
    }

    public String getKey() {
        return Key;
    }
}
