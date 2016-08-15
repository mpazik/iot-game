package dzida.server.app.network;

public interface Connection {
    void disconnect();

    void send(String data);
}
