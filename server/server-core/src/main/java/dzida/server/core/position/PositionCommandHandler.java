package dzida.server.core.position;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.pathfinding.PathFinder;

import java.util.Collections;
import java.util.List;

public class PositionCommandHandler {
    private final CharacterService characterService;
    private final PositionService positionService;
    private final TimeService timeService;
    private final PathFinder pathFinder;

    public PositionCommandHandler(CharacterService characterService, PositionService positionService, TimeService timeService, PathFinder pathFinder) {
        this.characterService = characterService;
        this.positionService = positionService;
        this.timeService = timeService;
        this.pathFinder = pathFinder;
    }

    public List<GameEvent> move(Id<Character> characterId, Point destination, double velocity) {
        if (!characterService.isCharacterLive(characterId)) {
            return Collections.emptyList();
        }
        Move move = positionService.getMove(characterId);
        Point currentPosition = move.getPositionAtTime(timeService.getCurrentMillis());
        List<Point> pathToDestination = pathFinder.findPathToDestination(currentPosition, destination);

        Point[] positions = pathToDestination.toArray(new Point[pathToDestination.size()]);
        Move newMove = move.continueMoveTo(timeService.getCurrentMillis(), velocity, positions);
        Move newCompactedMove = newMove.compactHistory(timeService.getCurrentMillis() - 1000);
        return Collections.singletonList(new CharacterMoved(characterId, newCompactedMove));
    }
}
