package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;

public interface Server {

    String getKey();

    void handleMessage(int connectionId, String message);

    Result handleConnection(int connectionId, ClientConnection clientConnection, String connectionData);

    void handleDisconnection(int connectionId);
}
