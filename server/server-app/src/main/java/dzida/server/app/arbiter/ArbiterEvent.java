package dzida.server.app.arbiter;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.Instance;
import dzida.server.app.user.User;

public interface ArbiterEvent {
    ImmutableSet<Class<?>> classes = ImmutableSet.of(
            SystemStarted.class,
            SystemClosed.class,
            InstanceStarted.class,
            InstanceClosed.class,
            UserJoinedInstance.class,
            UserLeftInstance.class
    );

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
