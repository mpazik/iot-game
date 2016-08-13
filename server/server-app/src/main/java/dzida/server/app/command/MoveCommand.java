package dzida.server.app.command;

import dzida.server.app.Instance;
import dzida.server.core.basic.unit.Point;

public class MoveCommand implements InstanceCommand {
    public final double x;
    public final double y;

    public MoveCommand(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point asPoint() {
        return new Point(x, y);
    }
}
