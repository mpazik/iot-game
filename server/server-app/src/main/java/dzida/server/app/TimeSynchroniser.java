package dzida.server.app;

import dzida.server.app.command.Command;
import dzida.server.core.event.GameEvent;
import dzida.server.core.time.TimeService;

public class TimeSynchroniser {

    private final TimeService timeService;

    public TimeSynchroniser(TimeService timeService) {
        this.timeService = timeService;
    }

    public TimeSyncResponse timeSync(TimeSyncRequest timeSyncRequest) {
        return new TimeSyncResponse(timeSyncRequest.clientTime, timeService.getCurrentMillis());
    }

    public static final class TimeSyncRequest implements Command {
        private final long clientTime;

        public TimeSyncRequest(long clientTime) {
            this.clientTime = clientTime;
        }

        public long getClientTime() {
            return clientTime;
        }
    }

    public static final class TimeSyncResponse implements GameEvent {
        private final long clientTime;
        private final long serverTime;

        public TimeSyncResponse(long clientTime, long serverTime) {
            this.clientTime = clientTime;
            this.serverTime = serverTime;
        }

        public long getClientTime() {
            return clientTime;
        }

        public long getServerTime() {
            return serverTime;
        }

        @Override
        public int getId() {
            return GameEvent.TimeSyncRes;
        }
    }
}
