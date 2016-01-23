package common.assertions;

import dzida.server.core.basic.unit.Move;
import dzida.server.core.basic.unit.Point;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.data.Offset;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MoveAssert extends AbstractAssert<MoveAssert, Move> {
    protected MoveAssert(Move actual) {
        super(actual, MoveAssert.class);
    }

    public MoveAssert hasPositionAtTime(long time, Point position) {
        Point countedPosition = actual.getPositionAtTime(time);
        if (!Objects.equals(countedPosition, position)) {
            failWithMessage("Expected move's position at time <%s> to be <%s> but was <%s>", time, position, countedPosition);
        }
        return this;
    }

    public MoveAssert hasAngleAtTime(long time, double angle) {
        assertThat(actual.getAngleAtTime(time)).as("angle at time").isCloseTo(angle, Offset.offset(0.001));
        return this;
    }
}