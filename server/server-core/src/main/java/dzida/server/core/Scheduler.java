package dzida.server.core;

import dzida.server.core.event.GameEvent;

import java.util.List;

public interface Scheduler {

    void schedule(Runnable command, long delay);

    void schedulePeriodically(Runnable command, long initDelay, long period);

    void scheduleGameEvents(List<GameEvent> event, long delay);
}
