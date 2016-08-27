package dzida.server.app.arbiter.event;

import dzida.server.app.instance.Instance;
import dzida.server.core.basic.entity.Key;

public class InstanceClosed implements ArbiterEvent {
    public final Key<Instance> instanceKey;

    public InstanceClosed(Key<Instance> instanceKey) {
        this.instanceKey = instanceKey;
    }
}
