package dzida.server.app.instance;

import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;

public interface InstanceStore {
    void saveEvent(Key<Instance> instanceKey, GameEvent gameEvent);
}
