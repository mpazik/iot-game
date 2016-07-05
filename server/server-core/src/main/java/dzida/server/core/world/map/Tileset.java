package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;

import java.util.Map;

public class Tileset {
    public final Key<Tileset> key;
    public final Map<Integer, TerrainTypes> terrains;

    public Tileset(Key<Tileset> key, Map<Integer, TerrainTypes> terrains) {
        this.key = key;
        this.terrains = terrains;
    }

    public enum TerrainTypes {
        WATER,
        SOIL,
        GRASS,
        WATER_GRASS,
        GRASS_SOIL,
    }
}
