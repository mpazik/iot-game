package dzida.server.app.instance;

import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectKind;

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
