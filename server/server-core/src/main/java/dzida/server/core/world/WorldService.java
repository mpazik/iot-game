package dzida.server.core.world;

import dzida.server.core.world.model.WorldState;

public class WorldService {

    public static final String Key = "world";

    private final WorldState worldState;

    private WorldService(WorldState worldState) {
        this.worldState = worldState;
    }

    static public WorldService create(WorldState worldState) {
        return new WorldService(worldState);
    }

    public WorldState getState() {
        return worldState;
    }

    public String getKey() {
        return Key;
    }
}
