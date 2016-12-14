package dzida.server.app.instance.position;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.CharacterService;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.event.CharacterMoved;
import dzida.server.app.time.TimeService;

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
