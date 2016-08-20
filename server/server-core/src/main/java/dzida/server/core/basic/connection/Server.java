package dzida.server.core.basic.connection;

public interface Server<T> {
    void onConnection(Connector<T> connector);
}
