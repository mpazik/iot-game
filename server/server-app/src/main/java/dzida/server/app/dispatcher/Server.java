package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.ClientConnection;

public interface Server {

    String getKey();

    default Result verifyConnection(String connectionData) {
        return Result.ok();
    }

    void onConnection(ClientConnection<String> clientConnection, String connectionData);
}
