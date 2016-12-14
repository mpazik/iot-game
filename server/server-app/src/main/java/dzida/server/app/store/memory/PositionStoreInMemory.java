package dzida.server.app.store.memory;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.position.PositionStore;

import java.util.HashMap;
import java.util.Map;

public class PositionStoreInMemory implements PositionStore {
    private final Map<Id<Character>, Point> positions = new HashMap<>();

    private final Point spawnPoint;

    public PositionStoreInMemory(Point spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public Point getCharacterPosition(Id<Character> characterId) {
        return positions.getOrDefault(characterId, spawnPoint);
    }

    @Override
    public void setPosition(Id<Character> characterId, Point position) {
        positions.put(characterId, position);
    }
}
