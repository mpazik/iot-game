package dzida.server.app.store.memory;

import dzida.server.core.character.CharacterId;
import dzida.server.core.position.PositionStore;
import dzida.server.core.position.model.Position;

import java.util.HashMap;
import java.util.Map;

public class PositionStoreInMemory implements PositionStore {
    private final Map<CharacterId, Position> positions = new HashMap<>();

    private final Position spawnPoint;

    public PositionStoreInMemory(Position spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public Position getCharacterPosition(CharacterId characterId) {
        return positions.getOrDefault(characterId, spawnPoint);
    }

    @Override
    public void setPosition(CharacterId characterId, Position position) {
        positions.put(characterId, position);
    }
}
