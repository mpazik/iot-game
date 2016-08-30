package dzida.server.app.protocol.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dzida.server.app.serialization.BasicJsonSerializer;
import dzida.server.app.serialization.MessageSerializer;

import javax.annotation.Nullable;

public final class JsonProtocol {
    private final Gson gson;
    private final MessageSerializer inputSerializer;
    private final MessageSerializer outputSerializer;

    private JsonProtocol(Gson gson, MessageSerializer inputSerializer, MessageSerializer outputSerializer) {
        this.gson = gson;
        this.inputSerializer = inputSerializer;
        this.outputSerializer = outputSerializer;
    }

    public static JsonProtocol create(ImmutableSet<Class<?>> inputMessageClasses, ImmutableSet<Class<?>> outputMessageClasses) {
        return create(BasicJsonSerializer.getSerializer(), inputMessageClasses, outputMessageClasses);
    }

    public static JsonProtocol create(Gson serializer, ImmutableSet<Class<?>> inputMessageClasses, ImmutableSet<Class<?>> outputMessageClasses) {
        MessageSerializer inputSerializer = MessageSerializer.create(serializer, inputMessageClasses);
        MessageSerializer outputSerializer = MessageSerializer.create(serializer, outputMessageClasses);
        return new JsonProtocol(serializer, inputSerializer, outputSerializer);
    }

    @Nullable
    public Object parseMessage(String jsonMessage) {
        JsonArray message = gson.fromJson(jsonMessage, JsonArray.class);
        String type = message.get(0).getAsString();
        assert inputSerializer.isSupportedTypeName(type) : "Unsupported message type: " + type;
        JsonElement data = message.get(1);
        return inputSerializer.parseEvent(data, type);
    }

    @Nullable
    public String serializeMessage(Object message) {
        assert outputSerializer.isSupportedType(message) : "Unsupported message type: " + outputSerializer.getMessageType(message);
        String type = outputSerializer.getMessageType(message);
        JsonElement data = outputSerializer.serializeMessageToJsonTree(message);
        return gson.toJson(ImmutableList.of(type, data));
    }
}
