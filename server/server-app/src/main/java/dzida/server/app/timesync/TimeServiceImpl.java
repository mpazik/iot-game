package dzida.server.app.timesync;

import dzida.server.app.Configuration;
import dzida.server.core.time.TimeService;

import java.time.Instant;

public class TimeServiceImpl implements TimeService {

    private static final long serverTimeOffset = Configuration.getServerTimeOffset();

    public static long getServerTime() {
        return Instant.now().toEpochMilli() + serverTimeOffset;
    }

    public static Instant getServerInstant() {
        return Instant.now().plusMillis(serverTimeOffset);
    }

    @Override
    public long getCurrentMillis() {
        return getServerTime();
    }
}
