package dzida.server.core.world.model;

import dzida.server.core.position.model.Position;

public class WorldState {
    private final String name;
    private final int width;
    private final int height;
    private final String tileset;
    private final Position spawnPoint;
    private final int[] tiles;

    public WorldState(String name, int width, int height, String tileset, Position spawnPoint, int[] tiles) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileset = tileset;
        this.spawnPoint = spawnPoint;
        this.tiles = tiles;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTileset() {
        return tileset;
    }

    public Position getSpawnPoint() {
        return spawnPoint;
    }

    public int[] getTiles() {
        return tiles;
    }
}
