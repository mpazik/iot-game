package dzida.server.app.arbiter;

import com.google.common.collect.ImmutableSet;
import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.Instance;

public interface ArbiterCommand {
    ImmutableSet<Class<?>> clientCommandClasses = ImmutableSet.of(
            Travel.class
    );

    ImmutableSet<Class<?>> serverCommandClasses = ImmutableSet.of(
            JoinToInstance.class
    );

    class Travel implements ArbiterCommand {
        public final String location;

        public Travel(String location) {
            this.location = location;
        }
    }

    class JoinToInstance implements ArbiterCommand {
        final Key<Instance> instanceKey;

        public JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }
}
