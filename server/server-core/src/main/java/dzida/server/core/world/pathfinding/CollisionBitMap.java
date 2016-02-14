package dzida.server.core.world.pathfinding;

import com.google.common.collect.ImmutableSet;
import dzida.server.core.basic.unit.BitMap;
import dzida.server.core.basic.unit.BitMap.ImmutableBitMap;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.world.map.Tileset;
import dzida.server.core.world.map.Tileset.TerrainTypes;
import dzida.server.core.world.map.WorldMap;

import java.util.Set;

public class CollisionBitMap {
    public static final Set<TerrainTypes> COLLISION_TERRAINS = ImmutableSet.of(TerrainTypes.WATER, TerrainTypes.WATER_GRASS);

    private final BitMap bitMap;

    public CollisionBitMap(BitMap bitMap) {
        this.bitMap = bitMap;
    }

    public boolean isColliding(int x, int y) {
        return bitMap.isSet(x, y);
    }

    public BitMap toBitMap() {
        return bitMap;
    }

    public boolean isColliding(Point point) {
        return isColliding(doubleToInt(point.getX()), doubleToInt(point.getY()));
    }

    private int doubleToInt(double num) {
        return Math.toIntExact(Math.round(num));
    }

    public static CollisionBitMap createForWorldMap(WorldMap worldMap, Tileset tileset) {
        int width = worldMap.getWidth();
        int height = worldMap.getHeight();
        ImmutableBitMap.Builder bitMapBuilder = ImmutableBitMap.builder(width, height);
        int[] tiles = worldMap.getTiles();

        for (int i = 0; i < tiles.length; i++) {
            bitMapBuilder.set(i % width, i /  height, isTileCollidable(tiles[i], tileset));
        }

        return new CollisionBitMap(bitMapBuilder.build());
    }

    private static boolean isTileCollidable(int tile, Tileset tileset) {
        return COLLISION_TERRAINS.contains(tileset.getTerrains().get(tile));
    }
}
