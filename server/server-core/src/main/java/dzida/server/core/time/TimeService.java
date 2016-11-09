package dzida.server.core.time;

import java.time.Instant;

public interface TimeService {
    long getCurrentMillis();

    Instant getCurrentTime();
}
