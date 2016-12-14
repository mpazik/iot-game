package dzida.server.app.basic.connection;

public interface ServerConnection<T> {

    void send(T data);

    void close();
}
