package dzida.server.app.instance.event;

import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;

import javax.annotation.Nonnull;
import javax.ws.rs.NotSupportedException;

public class ServerMessage implements GameEvent {
    private final String message;

    public ServerMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Nonnull
    @Override
    public GameState updateState(@Nonnull GameState state, GameDefinitions definitions) {
        throw new NotSupportedException();
    }
}
