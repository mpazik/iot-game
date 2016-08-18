package dzida.server.core.event;

public class ServerMessage implements GameEvent {
    private final String message;
    private final int type;

    public ServerMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public static ServerMessage info(String message) {
        return new ServerMessage(message, Types.INFO);
    }

    public static ServerMessage error(String message) {
        return new ServerMessage(message, Types.ERROR);
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public static class Types {
        public final static int INFO = 0;
        public final static int ERROR = 1;
    }
}
