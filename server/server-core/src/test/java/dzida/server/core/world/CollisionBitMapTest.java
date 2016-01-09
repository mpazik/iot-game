package dzida.server.core.world;

import com.google.common.collect.ImmutableMap;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.world.model.Tileset;
import dzida.server.core.world.model.WorldMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollisionBitMapTest {
    public static final Tileset.TerrainTypes NON_COLLIDABLE_TERRAIN = Tileset.TerrainTypes.GRASS;
    public static final Tileset.TerrainTypes COLLIDABLE_TERRAIN = Tileset.TerrainTypes.WATER;

    @Test
    public void collidableAndNonCollidableTerrainsForTestsArePickedCorrectly() {
        assertThat(CollisionBitMap.COLLISION_TERRAINS).contains(COLLIDABLE_TERRAIN);
        assertThat(CollisionBitMap.COLLISION_TERRAINS).doesNotContain(NON_COLLIDABLE_TERRAIN);
    }

    @Test
    public void createdBitMapHasCollidableTilesSet() {
        Key<Tileset> tilesetKey = new Key<>("tileset-key");
        Tileset tileset = new Tileset(tilesetKey, ImmutableMap.of(1, NON_COLLIDABLE_TERRAIN, 2, COLLIDABLE_TERRAIN));
        int[] tiles = {
                2, 2, 1, 2,
                1, 1, 1, 1,
                2, 1, 1, 2,
                2, 2, 2, 2
        };
        WorldMap worldMap = new WorldMap(4, 4, tilesetKey, new Point(0, 0), tiles);

        CollisionBitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, tileset);
        assertThat(collisionBitMap.isColliding(0, 0)).isTrue();
        assertThat(collisionBitMap.isColliding(2, 0)).isFalse();
        assertThat(collisionBitMap.isColliding(3, 0)).isTrue();
        assertThat(collisionBitMap.isColliding(0, 1)).isFalse();
        assertThat(collisionBitMap.isColliding(3, 3)).isTrue();
    }
}