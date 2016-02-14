package dzida.server.core.world.event;

import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.event.GameEvent;
import dzida.server.core.world.object.WorldObject;
import lombok.Value;

@Value
public class WorldObjectCreated implements GameEvent {
    GeneralEntity<WorldObject> worldObject;

    @Override
    public int getId() {
        return GameEvent.WorldObjectCreated;
    }
}
