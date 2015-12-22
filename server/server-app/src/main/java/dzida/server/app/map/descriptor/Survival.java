package dzida.server.app.map.descriptor;

import dzida.server.core.position.model.Position;
import lombok.Value;

import java.util.List;

public class Survival implements Scenario {
    private final String type;
    private final String mapName;
    private final List<Spawn> spawns;
    private final int difficultyLevel;

    public Survival(String mapName, int difficultyLevel, List<Spawn> spawns) {
        this.mapName = mapName;
        this.spawns = spawns;
        this.difficultyLevel = difficultyLevel;
        this.type = "survival";
    }

    public List<Spawn> getSpawns() {
        return spawns;
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    @Override
    public String getType() {
        return type;
    }

    @Value
    public static class Spawn {
        private final Position position;
    }
}
