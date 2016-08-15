package dzida.server.app.network;

public interface ConnectionHandler {

    void handleConnection(int connectionId, Connection connectionController);

    void handleMessage(int connectionId, String message);

    void handleDisconnection(int connectionId);
}
