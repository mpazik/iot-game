package dzida.server.core.assertions;

import dzida.server.core.basic.unit.Move;

public class Assertions {
    public static MoveAssert assertThat(Move actual) {
        return new MoveAssert(actual);
    }

}
