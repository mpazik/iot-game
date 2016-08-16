package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;

public interface Server {

    String getKey();

    default Result verifyConnection(int connectionId, String connectionData) {
        return Result.ok();
    }

    void handleMessage(int connectionId, String message);

    void handleConnection(int connectionId, ClientConnection clientConnection, String connectionData);

    void handleDisconnection(int connectionId);
}
