package dzida.server.core.world.map;

import dzida.server.core.basic.entity.Key;
import lombok.Value;

import java.util.Map;

@Value
public class Tileset {
    final Key<Tileset> key;
    final Map<Integer, TerrainTypes> terrains;

    public enum TerrainTypes {
        WATER,
        SOIL,
        GRASS,
        WATER_GRASS,
        GRASS_SOIL,
    }
}
