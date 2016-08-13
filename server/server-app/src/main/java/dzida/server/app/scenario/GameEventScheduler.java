package dzida.server.app.scenario;

import dzida.server.app.InstanceStateManager;
import dzida.server.core.Scheduler;
import dzida.server.core.event.GameEvent;

import java.util.List;

public class GameEventScheduler {

    private final InstanceStateManager instanceStateManager;
    private final Scheduler scheduler;

    public GameEventScheduler(InstanceStateManager instanceStateManager, Scheduler scheduler) {
        this.instanceStateManager = instanceStateManager;
        this.scheduler = scheduler;
    }

    public void schedule(List<GameEvent> events, int delay) {
        scheduler.schedule(() -> instanceStateManager.dispatchEvents(events), delay);
    }

    public void dispatch(List<GameEvent> events) {
        instanceStateManager.dispatchEvents(events);
    }
}
