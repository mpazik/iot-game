package dzida.server.app.serialization;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageSerializer {
    private final Gson serializer;
    private final Map<String, Class<?>> messageClasses;

    private MessageSerializer(Gson serializer, Map<String, Class<?>> messageClasses) {
        this.serializer = serializer;
        this.messageClasses = messageClasses;
    }

    private static String getMessageTypeFromClass(Class<?> event) {
        return event.getSimpleName();
    }

    public static MessageSerializer create(ImmutableSet<Class<?>> messageClasses) {
        return create(BasicJsonSerializer.getSerializer(), messageClasses);
    }

    public static MessageSerializer create(Gson serializer, ImmutableSet<Class<?>> messageClasses) {
        Map<String, Class<?>> messageClassesMap = new HashMap<>();
        messageClasses.forEach(messageClass -> {
            String messageType = getMessageTypeFromClass(messageClass);
            if (messageClassesMap.containsKey(messageType)) {
                if (messageClassesMap.get(messageType).equals(messageClass)) {
                    throw new RuntimeException("Registered same event class twice. Duplicated class: " + messageClass.getName());
                } else {
                    throw new RuntimeException("Two different classes have same name. " +
                            "Remember that name of the old class name can not be changed. " +
                            "Duplicated classes: " + messageClassesMap.get(messageType).getName() + " & " + messageClass.getName());
                }
            }
            messageClassesMap.put(messageType, messageClass);
        });
        return new MessageSerializer(serializer, ImmutableMap.copyOf(messageClassesMap));
    }

    public Object parseEvent(String data, String type) {
        return parseSafe((eventClass) -> serializer.fromJson(data, eventClass), type);
    }

    public Object parseEvent(JsonElement data, String type) {
        return parseSafe((eventClass) -> serializer.fromJson(data, eventClass), type);
    }

    public Object parseSafe(Function<Class<?>, Object> parse, String type) {
        try {
            Class<?> eventClass = messageClasses.get(type);
            if (eventClass == null) {
                return null;
            }
            return parse.apply(eventClass);
        } catch (JsonSyntaxException | IllegalStateException | UnsupportedOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String serializeMessage(Object message) {
        return serializer.toJson(message);
    }

    public JsonElement serializeMessageToJsonTree(Object message) {
        return serializer.toJsonTree(message);
    }

    public String getMessageType(Object message) {
        return getMessageType(message.getClass());
    }

    public String getMessageType(Class<?> messageClass) {
        return getMessageTypeFromClass(messageClass);
    }

    public Class<?> getMessageClass(String messageType) {
        return messageClasses.get(messageType);
    }

    public boolean isSupportedType(Object message) {
        return isSupportedType(message.getClass());
    }

    public boolean isSupportedType(Class<?> messageClass) {
        return isSupportedTypeName(getMessageTypeFromClass(messageClass));
    }

    public boolean isSupportedTypeName(String type) {
        return messageClasses.containsKey(type);
    }
}
