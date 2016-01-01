package dzida.server.core.position;

import dzida.server.core.character.CharacterId;
import dzida.server.core.position.model.Point;

public interface PositionStore {
    Point getCharacterPosition(CharacterId characterId);

    void setPosition(CharacterId characterId, Point position);
}
