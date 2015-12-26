package dzida.server.app;

import com.google.common.reflect.TypeToken;
import dzida.server.core.position.model.Position;
import dzida.server.core.world.model.WorldState;
import lombok.Value;

public class WorldStateStore {
    private final StaticDataLoader staticDataLoader = new StaticDataLoader();

    public WorldState loadMap(String name) {
        WorldMap worldMap = staticDataLoader.loadJsonFromServer("maps/" + name + ".json", TypeToken.of(WorldMap.class));
        Position spawnPoint = Position.of(worldMap.spawnPoint.x, worldMap.spawnPoint.y);
        return new WorldState(name, worldMap.width, worldMap.height, worldMap.tileset, spawnPoint, worldMap.tiles);
    }

    @Value
    private static final class WorldMap {
        int width;
        int height;
        String tileset;
        PositionBean spawnPoint;
        int[] tiles;
    }

    @Value
    private static final class PositionBean {
        double x;
        double y;
    }
}
