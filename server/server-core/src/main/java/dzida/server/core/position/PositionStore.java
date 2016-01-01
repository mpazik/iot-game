package dzida.server.core.position;

import dzida.server.core.character.CharacterId;
import dzida.server.core.basic.unit.Point;

public interface PositionStore {
    Point getCharacterPosition(CharacterId characterId);

    void setPosition(CharacterId characterId, Point position);
}
