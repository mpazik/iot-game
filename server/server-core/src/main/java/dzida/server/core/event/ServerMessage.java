package dzida.server.core.event;

public class ServerMessage implements GameEvent {
    private final String message;

    public ServerMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
