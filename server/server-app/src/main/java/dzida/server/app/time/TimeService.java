package dzida.server.app.time;

import java.time.Instant;

public interface TimeService {
    long getCurrentMillis();

    Instant getCurrentTime();
}
