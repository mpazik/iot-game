package dzida.server.app.scenario;

import dzida.server.app.GameEventDispatcher;
import dzida.server.core.Scheduler;
import dzida.server.core.event.GameEvent;

import java.util.List;

public class GameEventScheduler {

    private final GameEventDispatcher gameEventDispatcher;
    private final Scheduler scheduler;

    public GameEventScheduler(GameEventDispatcher gameEventDispatcher, Scheduler scheduler) {
        this.gameEventDispatcher = gameEventDispatcher;
        this.scheduler = scheduler;
    }

    public void schedule(List<GameEvent> events, int delay) {
        scheduler.schedule(() -> gameEventDispatcher.dispatchEvents(events), delay);
    }

    public void dispatch(List<GameEvent> events) {
        gameEventDispatcher.dispatchEvents(events);
    }
}
