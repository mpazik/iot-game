package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;

public class WorldMap {
    private final int width;
    private final int height;
    private final Key<Tileset> tileset;
    private final Point spawnPoint;
    private final int[] tiles;
    private final int firstgid;

    public WorldMap(int width, int height, Key<Tileset> tileset, Point spawnPoint, int[] tiles, int firstgid) {
        this.width = width;
        this.height = height;
        this.tileset = tileset;
        this.spawnPoint = spawnPoint;
        this.tiles = tiles;
        this.firstgid = firstgid;
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
