package dzida.server.app.scenario;

import dzida.server.app.command.InstanceCommand;
import dzida.server.core.Scheduler;

import java.util.function.Consumer;

public class InstanceCommandScheduler {

    private final Consumer<InstanceCommand> instanceCommandConsumer;
    private final Scheduler scheduler;

    public InstanceCommandScheduler(Consumer<InstanceCommand> instanceCommandConsumer, Scheduler scheduler) {
        this.instanceCommandConsumer = instanceCommandConsumer;
        this.scheduler = scheduler;
    }

    public void schedule(InstanceCommand command, int delay) {
        scheduler.schedule(() -> instanceCommandConsumer.accept(command), delay);
    }
}
