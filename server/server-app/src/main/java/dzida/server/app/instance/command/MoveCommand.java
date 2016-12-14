package dzida.server.app.instance.command;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.model.Character;

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

    public Point asPoint() {
        return new Point(x, y);
    }
}
