package dzida.server.core.position;

import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.position.model.Move;
import dzida.server.core.position.model.Point;
import dzida.server.core.time.TimeService;

import java.util.Collections;
import java.util.List;

public class PositionCommandHandler {
    private final CharacterService characterService;
    private final PositionService positionService;
    private final TimeService timeService;

    public PositionCommandHandler(CharacterService characterService, PositionService positionService, TimeService timeService) {
        this.characterService = characterService;
        this.positionService = positionService;
        this.timeService = timeService;
    }

    public List<GameEvent> move(CharacterId characterId, Point position, double velocity) {
        if (!characterService.isCharacterLive(characterId)) {
            return Collections.emptyList();
        }
        Move newMove = positionService.getMove(characterId).continueMoveTo(timeService.getCurrentMillis(), velocity, position);
        Move newCompactedMove = newMove.compactHistory(timeService.getCurrentMillis() - 1000);
        return Collections.singletonList(new CharacterMoved(characterId, newCompactedMove));
    }
}
