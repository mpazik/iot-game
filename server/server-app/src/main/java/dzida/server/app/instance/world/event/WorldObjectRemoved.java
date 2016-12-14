package dzida.server.app.instance.world.event;

import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.world.object.WorldObject;

public class WorldObjectRemoved implements GameEvent {
    public final GeneralEntity<WorldObject> worldObject;


    public WorldObjectRemoved(GeneralEntity<WorldObject> worldObject) {
        this.worldObject = worldObject;
    }
}
