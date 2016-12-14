package dzida.server.app.basic.connection;

public interface Server<T> {
    void onConnection(Connector<T> connector);
}
