package dzida.server.core.basic.connection;

public interface ConnectionServer<T> {
    void onConnection(ClientConnection<T> clientConnection);
}
