package dzida.server.app;

import dzida.server.core.Scheduler;
import dzida.server.core.event.GameEvent;
import io.netty.channel.EventLoop;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SchedulerImpl implements Scheduler {

    private final EventLoop eventLoop;
    private final GameEventDispatcher gameEventDispatcher;

    public SchedulerImpl(EventLoop eventLoop, GameEventDispatcher gameEventDispatcher) {
        this.eventLoop = eventLoop;
        this.gameEventDispatcher = gameEventDispatcher;
    }

    @Override
    public void schedule(Runnable command, long delay) {
        eventLoop.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void schedulePeriodically(Runnable command, long initDelay, long period) {
        eventLoop.scheduleWithFixedDelay(command, initDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleGameEvents(List<GameEvent> events, long delay) {
        schedule(() -> gameEventDispatcher.dispatchEvents(events), delay);
    }
}
