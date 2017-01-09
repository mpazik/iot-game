package dzida.server.app.instance.command;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.event.CharacterMoved;

import java.util.List;

public class MoveCommand implements InstanceCommand {
    public final double x;
    public final double y;
    public final Id<Character> characterId;
    public final double speed;

    public MoveCommand(Id<Character> characterId, double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.characterId = characterId;
        this.speed = speed;
    }

    @Override
    public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
        if (!state.getCharacter().isCharacterLive(characterId)) {
            return Outcome.ok(ImmutableList.of());
        }
        Point destination = new Point(x, y);
        double speed = this.speed == 0 ? definitions.getPlayerSpeed() : this.speed;

        Move move = state.getPosition().getMove(characterId);
        List<Point> pathToDestination = state.getPosition().findPathToDestination(characterId, destination, currentTime);

        Point[] positions = pathToDestination.toArray(new Point[pathToDestination.size()]);
        Move newMove = move.continueMoveTo(currentTime, speed, positions);
        Move newCompactedMove = newMove.compactHistory(currentTime - 1000);
        return Outcome.ok(ImmutableList.of(new CharacterMoved(characterId, newCompactedMove)));
    }
}
