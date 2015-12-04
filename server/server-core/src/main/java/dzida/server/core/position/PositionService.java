package dzida.server.core.position;

import dzida.server.core.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.position.model.Move;
import dzida.server.core.position.model.Position;
import dzida.server.core.time.TimeService;

import java.util.List;

public interface PositionService {
    double BotSpeed = 1.0;
    double PlayerSpeed = 4.0;

    static PositionService create(PositionStore positionStore, TimeService timeService) {
        return new PositionServiceImpl(positionStore, timeService);
    }

    List<CharacterMoved> getState();

    String getKey();

    boolean areCharactersInDistance(CharacterId character1, CharacterId character2, double distance, long time);

    void processEvent(GameEvent gameEvent);

    Move getMove(CharacterId characterId);

    Move getInitialMove(CharacterId characterId);

    Position getPosition(CharacterId characterId, long currentMillis);

    boolean isStanding(CharacterId characterId, long currentMillis);
}
