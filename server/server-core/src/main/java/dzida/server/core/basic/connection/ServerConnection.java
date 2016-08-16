package dzida.server.core.basic.connection;

public interface ServerConnection<T> {

    void send(T message);

    void close();
}
