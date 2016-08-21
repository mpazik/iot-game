package dzida.server.app.arbiter;

import dzida.server.app.BasicJsonSerializer;
import dzida.server.app.instance.Instance;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

public class ArbiterProtocol {

    public static JsonProtocol createSerializer() {
        return new JsonProtocol.Builder()
                .registerParsingMessageType(1, JoinBattleCommand.class)
                .registerParsingMessageType(2, GoHomeCommand.class)
                .registerSerializationMessageType(1, JoinToInstance.class)
                .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                .registerTypeHierarchyAdapter(Key.class, BasicJsonSerializer.keyTypeAdapter)
                .build();
    }
}

class JoinBattleCommand {
    public final String map;
    public final int difficultyLevel;

    public JoinBattleCommand(String map, int difficultyLevel) {
        this.map = map;
        this.difficultyLevel = difficultyLevel;
    }
}

class GoHomeCommand {
}

class JoinToInstance {
    final Key<Instance> instanceKey;

    public JoinToInstance(Key<Instance> instanceKey) {
        this.instanceKey = instanceKey;
    }
}
