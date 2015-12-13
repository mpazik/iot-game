package dzida.server.app.map.descriptor;

import dzida.server.core.position.model.Position;
import lombok.Value;

import java.util.List;

public class Survival implements Scenario {
    private final String mapName;
    private final List<Spawn> spawns;

    public Survival(String mapName, List<Spawn> spawns) {
        this.mapName = mapName;
        this.spawns = spawns;
    }

    public List<Spawn> getSpawns() {
        return spawns;
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Value
    public static class Spawn {
        private final Position position;
        private final int botType;
    }
}
