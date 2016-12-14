package dzida.server.app;

import java.time.Duration;

public interface Scheduler {

    void schedule(Runnable command, long delay);
    void schedule(Runnable command, Duration delay);

    void schedulePeriodically(Runnable command, long initDelay, long period);
}
