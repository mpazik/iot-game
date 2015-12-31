package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import dzida.server.core.position.model.Position;
import dzida.server.core.world.model.WorldMap;
import lombok.Value;

public class WorldMapLoader {
    private final StaticDataLoader staticDataLoader;

    public WorldMapLoader(StaticDataLoader staticDataLoader) {
        this.staticDataLoader = staticDataLoader;
    }

    public WorldMap loadMap(String name) {
        WorldMapBean worldMapBean = staticDataLoader.loadJsonFromServer("maps/" + name + ".json", TypeToken.of(WorldMapBean.class));
        Position spawnPoint = Position.of(worldMapBean.spawnPoint.x, worldMapBean.spawnPoint.y);
        return new WorldMap(name, worldMapBean.width, worldMapBean.height, worldMapBean.tileset, spawnPoint, worldMapBean.tiles);
    }

    @Value
    private static final class WorldMapBean {
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
