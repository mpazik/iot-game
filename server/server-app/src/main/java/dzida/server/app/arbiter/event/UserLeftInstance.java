package dzida.server.app.arbiter.event;

import dzida.server.app.instance.Instance;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

public class UserLeftInstance implements ArbiterEvent {
    public final Key<Instance> instanceKey;
    public final Id<User> userId;

    public UserLeftInstance(Key<Instance> instanceKey, Id<User> userId) {
        this.instanceKey = instanceKey;
        this.userId = userId;
    }
}
