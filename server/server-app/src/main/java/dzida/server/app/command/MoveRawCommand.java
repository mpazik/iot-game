package dzida.server.app.command;

import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.MoveCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public class MoveRawCommand implements CharacterCommand {
    public final double x;
    public final double y;
    public final double speed;

    public MoveRawCommand(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    @Override
    public InstanceCommand getInstanceCommand(Id<Character> characterId) {
        return new MoveCommand(characterId, x, y, speed);
    }
}
