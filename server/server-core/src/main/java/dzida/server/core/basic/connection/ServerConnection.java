package dzida.server.core.basic.connection;

public interface ServerConnection<T> {

    void send(T data);

    void close();
}
