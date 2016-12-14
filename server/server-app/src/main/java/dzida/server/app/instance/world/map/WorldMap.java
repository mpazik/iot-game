package dzida.server.app.instance.world.map;

import dzida.server.app.basic.entity.Key;
import dzida.server.app.basic.unit.Point;

public class WorldMap {
    private final int width;
    private final int height;
    private final Key<Tileset> tileset;
    private final Point spawnPoint;
    private final int[] tiles;
    private final int firstgid;
    @SuppressWarnings("FieldCanBeLocal") // used in client side
    private final String backgroundColor;

    public WorldMap(int width, int height, Key<Tileset> tileset, Point spawnPoint, int[] tiles, int firstgid, String backgroundColor) {
        this.width = width;
        this.height = height;
        this.tileset = tileset;
        this.spawnPoint = spawnPoint;
        this.tiles = tiles;
        this.firstgid = firstgid;
        this.backgroundColor = backgroundColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Key<Tileset> getTileset() {
        return tileset;
    }

    public Point getSpawnPoint() {
        return spawnPoint;
    }

    public int[] getTiles() {
        return tiles;
    }

    public int getFirstgid() {
        return firstgid;
    }
}
