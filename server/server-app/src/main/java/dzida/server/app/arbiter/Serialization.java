package dzida.server.app.arbiter;

import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.app.serialization.BasicJsonSerializer;
import dzida.server.app.serialization.MessageSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

public class Serialization {

    public static JsonProtocol createSerializer() {
        return new JsonProtocol.Builder()
                .registerParsingMessageType(1, ArbiterCommand.JoinBattleCommand.class)
                .registerParsingMessageType(2, ArbiterCommand.GoHomeCommand.class)
                .registerSerializationMessageType(1, ArbiterCommand.JoinToInstance.class)
                .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                .registerTypeHierarchyAdapter(Key.class, BasicJsonSerializer.keyTypeAdapter)
                .build();
    }

    public static MessageSerializer createArbiterEventSerializer() {
        return new MessageSerializer.Builder()
                .registerMessage(ArbiterEvent.SystemStarted.class)
                .registerMessage(ArbiterEvent.SystemClosed.class)
                .registerMessage(ArbiterEvent.InstanceStarted.class)
                .registerMessage(ArbiterEvent.InstanceClosed.class)
                .registerMessage(ArbiterEvent.UserJoinedInstance.class)
                .registerMessage(ArbiterEvent.UserLeftInstance.class)
                .build();
    }
}

