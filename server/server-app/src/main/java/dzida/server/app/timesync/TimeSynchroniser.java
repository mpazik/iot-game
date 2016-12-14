package dzida.server.app.timesync;

import dzida.server.app.basic.Result;
import dzida.server.app.basic.connection.Connector;
import dzida.server.app.basic.connection.ServerConnection;
import dzida.server.app.basic.connection.VerifyingConnectionServer;
import dzida.server.app.time.TimeService;

public class TimeSynchroniser implements VerifyingConnectionServer<String, String> {

    private final TimeService timeService;

    public TimeSynchroniser(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public Result onConnection(Connector<String> connector, String connectionData) {
        connector.onOpen(new ServerConnection<String>() {
            @Override
            public void send(String data) {
                connector.onMessage(data + " " + timeService.getCurrentMillis());
            }

            @Override
            public void close() {

            }
        });
        return Result.ok();
    }
}
