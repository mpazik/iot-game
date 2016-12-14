package dzida.server.app.instance.world.pathfinding;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.BitMap.ImmutableBitMap;
import dzida.server.app.instance.world.map.Tileset;
import dzida.server.app.instance.world.map.Tileset.TerrainType;
import dzida.server.app.instance.world.map.WorldMap;

import java.util.Set;

public class CollisionBitMap {
    public static final Set<TerrainType> COLLISION_TERRAINS = ImmutableSet.of(
            TerrainType.WATER,
            TerrainType.WATER_GRASS,
            TerrainType.LAVA,
            TerrainType.VOID
    );

    public static BitMap createForWorldMap(WorldMap worldMap, Tileset tileset) {
        int width = worldMap.getWidth();
        int height = worldMap.getHeight();
        ImmutableBitMap.Builder bitMapBuilder = ImmutableBitMap.builder(width, height);
        int[] tiles = worldMap.getTiles();

        for (int i = 0; i < tiles.length; i++) {
            int tile = tiles[i] - worldMap.getFirstgid();
            bitMapBuilder.set(i % width, i / height, isTileCollidable(tile, tileset));
        }

        return bitMapBuilder.build();
    }

    private static boolean isTileCollidable(int tile, Tileset tileset) {
        return COLLISION_TERRAINS.contains(tileset.terrains.get(tile));
    }
}
