package dzida.server.core.world;

import com.google.common.collect.ImmutableSet;
import dzida.server.core.world.model.Tileset;
import dzida.server.core.world.model.Tileset.TerrainTypes;
import dzida.server.core.world.model.WorldMap;

import java.util.BitSet;
import java.util.Set;

public class CollisionBitMap {
    public static final Set<TerrainTypes> COLLISION_TERRAINS = ImmutableSet.of(TerrainTypes.WATER, TerrainTypes.WATER_GRASS);

    private final BitSet bitSet;
    private final int width;

    public CollisionBitMap(int width) {
        bitSet = new BitSet();
        this.width = width;
    }

    public boolean isColliding(int x, int y) {
        return bitSet.get(bitSetPos(x, y));
    }

    private void setCollision(int x, int y) {
        bitSet.set(bitSetPos(x, y));
    }

    private void clearCollision(int x, int y) {
        bitSet.clear(bitSetPos(x, y));
    }

    private int bitSetPos(int x, int y) {
        return y * width + x;
    }

    public static CollisionBitMap createForWorldMap(WorldMap worldMap, Tileset tileset) {
        CollisionBitMap collisionBitMap = new CollisionBitMap(worldMap.getWidth());
        int[] tiles = worldMap.getTiles();

        for (int i = 0; i < tiles.length; i++){
            collisionBitMap.bitSet.set(i, isTileCollidable(tiles[i], tileset));
        }

        return collisionBitMap;
    }

    private static boolean isTileCollidable(int tile, Tileset tileset) {
        return COLLISION_TERRAINS.contains(tileset.getTerrains().get(tile));
    }
}
