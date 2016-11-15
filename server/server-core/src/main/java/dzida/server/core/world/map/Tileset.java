package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;

import java.util.Map;

public class Tileset {
    public final Key<Tileset> key;
    public final Map<Integer, TerrainType> terrains;

    public Tileset(Key<Tileset> key, Map<Integer, TerrainType> terrains) {
        this.key = key;
        this.terrains = terrains;
    }

    public enum TerrainType {
        WATER,
        SOIL,
        GRASS,
        WATER_GRASS,
        LAVA,
        VOID,
        OTHER
    }
}
