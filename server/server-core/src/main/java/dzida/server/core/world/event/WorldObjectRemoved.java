package dzida.server.core.world.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.event.GameEvent;
import dzida.server.core.world.object.WorldObject;

public class WorldObjectRemoved implements GameEvent {
    public final Id<WorldObject> worldObjectId;

    public WorldObjectRemoved(Id<WorldObject> worldObjectId) {
        this.worldObjectId = worldObjectId;
    }

    @Override
    public int getId() {
        return GameEvent.WorldObjectRemoved;
    }
}
