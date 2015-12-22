package dzida.server.core;

public interface Scheduler {

    void schedule(Runnable command, long delay);

    void schedulePeriodically(Runnable command, long initDelay, long period);
}
