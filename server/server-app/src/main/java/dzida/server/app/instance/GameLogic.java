package dzida.server.app.instance;

import dzida.server.app.Scheduler;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.instance.world.event.WorldObjectCreated;
import dzida.server.app.instance.world.event.WorldObjectRemoved;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;

import java.time.Duration;

public class GameLogic {
    private final InstanceStateManager state;
    private final Scheduler scheduler;

    public GameLogic(
            Scheduler scheduler,
            InstanceStateManager state) {
        this.state = state;
        this.scheduler = scheduler;
    }

    public void start() {
        state.getEventPublisher().subscribe(event -> {
            if (!(event instanceof WorldObjectCreated)) {
                return;
            }
            WorldObjectCreated worldObjectCreated = (WorldObjectCreated) event;
            GeneralEntity<WorldObject> worldObject = worldObjectCreated.worldObject;
            WorldObjectKind kind = state.getWorldObjectService().getObjectKind(worldObject.getData().getKind());
            if (kind.getDecay() > 0) {
                scheduler.schedule(() -> {
                    state.dispatchEvent(new WorldObjectRemoved(worldObject));
                }, Duration.ofSeconds(kind.getDecay()));
            }
        });
    }
}
