package dzida.server.app.instance.position;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.event.CharacterMoved;
import dzida.server.app.instance.world.object.WorldObjectStore;
import dzida.server.app.time.TimeService;

import java.util.List;

public interface PositionService {
    double BotSpeed = 1.0;
    double PlayerSpeed = 4.0;

    static PositionService create(PositionStore positionStore, TimeService timeService, WorldObjectStore worldObjectStore, BitMap worldCollisionBitMap) {
        return new PositionServiceImpl(positionStore, timeService, worldObjectStore, worldCollisionBitMap);
    }

    List<CharacterMoved> getState();

    String getKey();

    boolean areCharactersInDistance(Id<Character> character1, Id<Character> character2, double distance, long time);

    void processEvent(GameEvent gameEvent);

    Move getMove(Id<Character> characterId);

    Move getInitialMove(Id<Character> characterId, Point spawnPoint);

    Point getPosition(Id<Character> characterId, long currentMillis);

    boolean isStanding(Id<Character> characterId, long currentMillis);

    List<Point> findPathToDestination(Id<Character> characterId, Point destination);
}
