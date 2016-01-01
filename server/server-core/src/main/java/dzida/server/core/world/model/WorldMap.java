package dzida.server.core.world.model;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.position.model.Point;

public class WorldMap {
    private final int width;
    private final int height;
    private final Key<Tileset> tileset;
    private final Point spawnPoint;
    private final int[] tiles;

    public WorldMap(int width, int height, Key<Tileset> tileset, Point spawnPoint, int[] tiles) {
        this.width = width;
        this.height = height;
        this.tileset = tileset;
        this.spawnPoint = spawnPoint;
        this.tiles = tiles;
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


}
