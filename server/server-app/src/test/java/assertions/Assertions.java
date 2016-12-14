package assertions;

import dzida.server.app.basic.unit.Move;

public class Assertions {
    public static MoveAssert assertThat(Move actual) {
        return new MoveAssert(actual);
    }

}
