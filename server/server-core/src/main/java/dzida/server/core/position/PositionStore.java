package dzida.server.core.position;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.model.Character;

public interface PositionStore {
    Point getCharacterPosition(Id<Character> characterId);

    void setPosition(Id<Character> characterId, Point position);
}
