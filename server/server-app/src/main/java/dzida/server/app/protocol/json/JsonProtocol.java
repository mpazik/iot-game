package dzida.server.app.protocol.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class JsonProtocol {
    private static final Logger log = Logger.getLogger(JsonProtocol.class);

    private final Gson gson;
    private final Map<Integer, Class<?>> parsingMessageTypes;
    private final Map<Class<?>, Integer> serializationMessageTypes;

    private JsonProtocol(Gson gson, Map<Integer, Class<?>> parsingMessageTypes, Map<Class<?>, Integer> serializationMessageTypes) {
        this.gson = gson;
        this.parsingMessageTypes = parsingMessageTypes;
        this.serializationMessageTypes = serializationMessageTypes;
    }

    @Nullable
    public Object parseMessage(String jsonMessage) {
        try {
            JsonArray message = gson.fromJson(jsonMessage, JsonArray.class);
            int type = message.get(0).getAsNumber().intValue();
            JsonElement data = message.get(1);
            Class<?> messageType = parsingMessageTypes.get(type);
            if (messageType == null) {
                return null;
            }
            return gson.fromJson(data, messageType);
        } catch (JsonSyntaxException | IllegalStateException | UnsupportedOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String serializeMessage(Object message) {
        //noinspection SuspiciousMethodCalls
        Integer typeCode = serializationMessageTypes.get(message.getClass());
        if (typeCode == null) {
            log.error("Trying to serialized not registered object: " + message);
            return null;
        }
        return gson.toJson(ImmutableList.of(typeCode, message));
    }

    public final static class Builder {
        private final Map<Integer, Class<?>> parsingMessageTypes = new HashMap<>();
        private final Map<Class<?>, Integer> serializationMessageTypes = new HashMap<>();
        private final GsonBuilder gsonBuilder = new GsonBuilder();

        public Builder registerMessageType(int typeCode, Class<?> messageType) {
            parsingMessageTypes.put(typeCode, messageType);
            serializationMessageTypes.put(messageType, typeCode);
            return this;
        }

        public Builder registerParsingMessageType(int typeCode, Class<?> messageType) {
            parsingMessageTypes.put(typeCode, messageType);
            return this;
        }

        public Builder registerSerializationMessageType(int typeCode, Class<?> messageType) {
            serializationMessageTypes.put(messageType, typeCode);
            return this;
        }

        public <T> Builder registerTypeHierarchyAdapter(Class<T> baseType, TypeAdapter<T> typeAdapter) {
            gsonBuilder.registerTypeHierarchyAdapter(baseType, typeAdapter);
            return this;
        }

        public <T> Builder registerTypeAdapter(Class<T> baseType, TypeAdapter<T> typeAdapter) {
            gsonBuilder.registerTypeAdapter(baseType, typeAdapter);
            return this;
        }

        public JsonProtocol build() {
            return new JsonProtocol(gsonBuilder.create(),
                    ImmutableMap.copyOf(parsingMessageTypes),
                    ImmutableMap.copyOf(serializationMessageTypes)
            );
        }
    }
}
