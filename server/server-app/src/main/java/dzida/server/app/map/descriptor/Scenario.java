package dzida.server.app.map.descriptor;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.map.WorldMap;

import java.util.Objects;

public final class Scenario {
    private final Key<WorldMap> worldMapKey;

    public Scenario(Key<WorldMap> worldMapKey) {
        this.worldMapKey = worldMapKey;
    }

    public Key<WorldMap> getWorldMapKey() {
        return worldMapKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return Objects.equals(worldMapKey, scenario.worldMapKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldMapKey);
    }
}
