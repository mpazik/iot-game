package dzida.server.app.store.memory;

import dzida.server.core.character.CharacterId;
import dzida.server.core.position.PositionStore;
import dzida.server.core.basic.unit.Point;

import java.util.HashMap;
import java.util.Map;

public class PositionStoreInMemory implements PositionStore {
    private final Map<CharacterId, Point> positions = new HashMap<>();

    private final Point spawnPoint;

    public PositionStoreInMemory(Point spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public Point getCharacterPosition(CharacterId characterId) {
        return positions.getOrDefault(characterId, spawnPoint);
    }

    @Override
    public void setPosition(CharacterId characterId, Point position) {
        positions.put(characterId, position);
    }
}
