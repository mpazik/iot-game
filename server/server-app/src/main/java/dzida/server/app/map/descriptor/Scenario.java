package dzida.server.app.map.descriptor;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.map.WorldMap;

public interface Scenario {

    Key<WorldMap> getWorldMapKey();
    String getType();
}
