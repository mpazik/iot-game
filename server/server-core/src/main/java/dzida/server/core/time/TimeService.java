package dzida.server.core.time;

import java.time.Instant;

public class TimeService {
    public long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }
}
