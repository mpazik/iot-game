package dzida.server.core.world.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.event.GameEvent;
import dzida.server.core.world.object.WorldObject;
import lombok.Value;

@Value
public class WorldObjectRemoved implements GameEvent {
    Id<WorldObject> worldObjectId;

    @Override
    public int getId() {
        return GameEvent.WorldObjectRemoved;
    }
}