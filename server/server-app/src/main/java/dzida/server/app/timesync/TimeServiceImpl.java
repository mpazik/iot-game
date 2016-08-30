package dzida.server.app.timesync;

import dzida.server.app.Configuration;
import dzida.server.core.time.TimeService;

import java.time.Instant;

public class TimeServiceImpl implements TimeService {

    private final long serverTimeOffset = Configuration.getServerTimeOffset();

    @Override
    public long getCurrentMillis() {
        return Instant.now().toEpochMilli() + serverTimeOffset;
    }
}
