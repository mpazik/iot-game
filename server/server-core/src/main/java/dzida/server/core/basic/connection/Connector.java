package dzida.server.core.basic.connection;

public interface Connector<T> {
    void onOpen(ServerConnection<T> serverConnection);

    void onClose();

    void onMessage(T data);
}
