package dzida.server.app;

import dzida.server.core.CharacterId;
import dzida.server.core.position.PositionStore;
import dzida.server.core.position.model.Position;

import java.util.HashMap;
import java.util.Map;

public class PositionStoreImpl implements PositionStore {
    private final Map<CharacterId, Position> positions = new HashMap<>();

    private final Position spawnPoint;

    public PositionStoreImpl(Position spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public Position getCharacterPosition(CharacterId characterId) {
        return positions.getOrDefault(characterId, spawnPoint);
    }

    public void setPosition(CharacterId characterId, Position position) {
        positions.put(characterId, position);
    }
}
