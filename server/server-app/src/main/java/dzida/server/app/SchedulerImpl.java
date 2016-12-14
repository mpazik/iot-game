package dzida.server.app;

import io.netty.channel.EventLoop;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SchedulerImpl implements Scheduler {

    private final EventLoop eventLoop;

    public SchedulerImpl(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void schedule(Runnable command, long delay) {
        eventLoop.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void schedule(Runnable command, Duration delay) {
        schedule(command, delay.getSeconds() * 1000);
    }

    @Override
    public void schedulePeriodically(Runnable command, long initDelay, long period) {
        eventLoop.scheduleWithFixedDelay(command, initDelay, period, TimeUnit.MILLISECONDS);
    }
}
