package dzida.server.app.timesync;

import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.ClientConnection;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.time.TimeService;

public class TimeSynchroniser implements VerifyingConnectionServer<String, String> {

    private final TimeService timeService;

    public TimeSynchroniser(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public Result verifyConnection(String connectionData) {
        return Result.ok();
    }

    @Override
    public void onConnection(ClientConnection<String> clientConnection, String connectionData) {
        clientConnection.onOpen(new ServerConnection<String>() {
            @Override
            public void send(String message) {
                clientConnection.onMessage(message + " " + timeService.getCurrentMillis());
            }

            @Override
            public void close() {

            }
        });
    }
}
