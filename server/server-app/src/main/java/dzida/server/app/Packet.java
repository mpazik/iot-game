package dzida.server.app;

public final class Packet {
    private final int type;
    private final Object data;

    public Packet(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
