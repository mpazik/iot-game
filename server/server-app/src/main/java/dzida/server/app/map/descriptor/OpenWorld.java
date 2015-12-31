package dzida.server.app.map.descriptor;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.model.WorldMap;

public class OpenWorld implements Scenario {
    private final String type;
    private final Key<WorldMap> worldMapKey;

    public OpenWorld(Key<WorldMap> worldMapKey) {
        this.worldMapKey = worldMapKey;
        this.type = "open-world";
    }

    public Key<WorldMap> getWorldMapKey() {
        return worldMapKey;
    }

    @Override
    public String getType() {
        return type;
    }
}
