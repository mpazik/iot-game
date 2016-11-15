package dzida.server.app.arbiter;

import dzida.server.app.instance.Instance;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

public interface ArbiterStore {

    void systemStarted();

    void systemStopped();

    void instanceStarted(Key<Instance> instanceKey);

    void instanceStopped(Key<Instance> instanceKey);

    void userJoinedInstance(Id<User> userId, Key<Instance> newInstanceKey);

    void playerLeftInstance(Id<User> userId, Key<Instance> lastInstanceKey);
}
