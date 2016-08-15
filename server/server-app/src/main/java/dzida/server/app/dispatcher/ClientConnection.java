package dzida.server.app.dispatcher;

public interface ClientConnection {
    void disconnect();

    void send(String data);
}
