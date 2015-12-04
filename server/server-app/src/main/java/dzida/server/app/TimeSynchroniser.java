package dzida.server.app;

import dzida.server.core.event.GameEvent;

import java.util.Date;

public class TimeSynchroniser {

    public TimeSyncResponse timeSync(TimeSyncRequest timeSyncRequest) {
        return new TimeSyncResponse(timeSyncRequest.clientTime, new Date().getTime());
    }

    public static final class TimeSyncRequest {
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
