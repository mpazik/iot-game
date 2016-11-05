package dzida.server.app.timesync;

import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.time.TimeService;

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
