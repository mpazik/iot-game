package dzida.server.app.map.descriptor;

import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.world.map.WorldMap;

import java.util.List;
import java.util.Set;

public class Survival implements Scenario {
    private final String type;
    private final Key<WorldMap> mapName;
    private final List<Spawn> spawns;
    private final Set<Id<User>> attendees;
    private final int difficultyLevel;

    public Survival(Key<WorldMap> mapName, int difficultyLevel, List<Spawn> spawns, Set<Id<User>> attendees) {
        this.mapName = mapName;
        this.spawns = spawns;
        this.difficultyLevel = difficultyLevel;
        this.attendees = attendees;
        this.type = "survival";
    }

    public List<Spawn> getSpawns() {
        return spawns;
    }

    public Key<WorldMap> getWorldMapKey() {
        return mapName;
    }

    public Set<Id<User>> getAttendees() {
        return attendees;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public Key<WorldMap> getMapName() {
        return mapName;
    }

    @Override
    public String getType() {
        return type;
    }

    public static class Spawn {
        public final Point position;

        public Spawn(Point position) {
            this.position = position;
        }
    }
}
