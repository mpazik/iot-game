package dzida.server.app.arbiter;

import dzida.server.app.instance.Instance;
import dzida.server.core.basic.entity.Key;

public interface ArbiterCommand {
    class GoHomeCommand implements ArbiterCommand {
    }

    class JoinBattleCommand implements ArbiterCommand {
        public final String map;
        public final int difficultyLevel;

        public JoinBattleCommand(String map, int difficultyLevel) {
            this.map = map;
            this.difficultyLevel = difficultyLevel;
        }
    }

    class JoinToInstance implements ArbiterCommand {
        final Key<Instance> instanceKey;

        public JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }
}
