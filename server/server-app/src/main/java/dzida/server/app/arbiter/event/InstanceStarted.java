package dzida.server.app.arbiter.event;

import dzida.server.app.instance.Instance;
import dzida.server.core.basic.entity.Key;

public class InstanceStarted implements ArbiterEvent {
    public final Key<Instance> instanceKey;

    public InstanceStarted(Key<Instance> instanceKey) {
        this.instanceKey = instanceKey;
    }
}
