package dzida.server.core.position;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.time.TimeService;

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

    public Outcome<List<GameEvent>> move(Id<Character> characterId, Point destination, double velocity) {
        if (!characterService.isCharacterLive(characterId)) {
            return Outcome.ok(ImmutableList.of());
        }
        Move move = positionService.getMove(characterId);
        List<Point> pathToDestination = positionService.findPathToDestination(characterId, destination);

        Point[] positions = pathToDestination.toArray(new Point[pathToDestination.size()]);
        Move newMove = move.continueMoveTo(timeService.getCurrentMillis(), velocity, positions);
        Move newCompactedMove = newMove.compactHistory(timeService.getCurrentMillis() - 1000);
        return Outcome.ok(ImmutableList.of(new CharacterMoved(characterId, newCompactedMove)));
    }
}
