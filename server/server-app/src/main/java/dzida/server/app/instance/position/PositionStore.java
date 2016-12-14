package dzida.server.app.instance.position;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.model.Character;

public interface PositionStore {
    Point getCharacterPosition(Id<Character> characterId);

    void setPosition(Id<Character> characterId, Point position);
}
