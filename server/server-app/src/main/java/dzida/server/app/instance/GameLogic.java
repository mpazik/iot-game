package dzida.server.app.instance;

import dzida.server.app.Scheduler;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.instance.world.WorldObjectCreated;
import dzida.server.app.instance.world.WorldObjectRemoved;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;

import java.time.Duration;

public class GameLogic {
    private final Scheduler scheduler;
    private final Instance instance;

    public GameLogic(
            Scheduler scheduler, Instance instance) {
        this.scheduler = scheduler;
        this.instance = instance;
    }

    public void start() {
        instance.subscribeChange((event -> {
            if (!(event instanceof WorldObjectCreated)) {
                return;
            }
            WorldObjectCreated worldObjectCreated = (WorldObjectCreated) event;
            GeneralEntity<WorldObject> worldObject = worldObjectCreated.getWorldObject();
            WorldObjectKind kind = instance.getGameDefinitions().getObjectKind(worldObject.getData().getKind());
            if (kind.getDecay() > 0) {
                scheduler.schedule(() -> {
                    instance.updateState(new WorldObjectRemoved(worldObject));
                }, Duration.ofSeconds(kind.getDecay()));
            }
        }));
    }
}
