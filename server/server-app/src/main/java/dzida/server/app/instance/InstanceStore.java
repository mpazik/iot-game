package dzida.server.app.instance;

import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.event.GameEvent;

public interface InstanceStore {
    void saveEvent(Key<Instance> instanceKey, GameEvent gameEvent);
}
