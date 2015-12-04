package dzida.server.app;

import dzida.server.core.position.model.Position;
import dzida.server.core.world.model.WorldState;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;

public class WorldStateStore {
    private static final URI staticServerAddress = URI.create("http://localhost:8080");

    public WorldState loadMap(String name) {
        WorldMap worldMap = getMapClient(name)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WorldMap.class);

        Position spawnPoint = Position.of(worldMap.spawnPoint.x, worldMap.spawnPoint.y);
        return new WorldState(name, worldMap.width, worldMap.height, worldMap.tileset, spawnPoint, worldMap.tiles);
    }

    private WebTarget getMapClient(String name) {
        return ClientBuilder.newClient()
                .target(staticServerAddress)
                .path("assets").path("maps").path(name + ".json");
    }

    private static final class WorldMap {
        private int width;
        private int height;
        private String tileset;
        private PositionBean spawnPoint;
        private int[] tiles;

        public WorldMap() {
        }

        public WorldMap(int width, int height, String tileset, PositionBean spawnPoint, int[] tiles) {
            this.width = width;
            this.height = height;
            this.tileset = tileset;
            this.spawnPoint = spawnPoint;
            this.tiles = tiles;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setTileset(String tileset) {
            this.tileset = tileset;
        }

        public void setSpawnPoint(PositionBean spawnPoint) {
            this.spawnPoint = spawnPoint;
        }

        public void setTiles(int[] tiles) {
            this.tiles = tiles;
        }
    }

    private static final class PositionBean {
        private double x;
        private double y;

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
