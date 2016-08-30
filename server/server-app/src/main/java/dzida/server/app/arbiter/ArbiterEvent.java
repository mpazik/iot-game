package dzida.server.app.arbiter;

import dzida.server.app.instance.Instance;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

public interface ArbiterEvent {
    class InstanceClosed implements ArbiterEvent {
        public final Key<Instance> instanceKey;

        public InstanceClosed(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    class InstanceStarted implements ArbiterEvent {
        public final Key<Instance> instanceKey;

        public InstanceStarted(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    class SystemClosed implements ArbiterEvent {
    }

    class SystemStarted implements ArbiterEvent {
    }

    class UserJoinedInstance implements ArbiterEvent {
        public final Key<Instance> instanceKey;
        public final Id<User> userId;

        public UserJoinedInstance(Key<Instance> instanceKey, Id<User> userId) {
            this.instanceKey = instanceKey;
            this.userId = userId;
        }
    }

    class UserLeftInstance implements ArbiterEvent {
        public final Key<Instance> instanceKey;
        public final Id<User> userId;

        public UserLeftInstance(Key<Instance> instanceKey, Id<User> userId) {
            this.instanceKey = instanceKey;
            this.userId = userId;
        }
    }
}
