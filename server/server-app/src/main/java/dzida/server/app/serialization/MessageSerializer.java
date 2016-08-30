package dzida.server.app.serialization;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MessageSerializer {
    private final Gson serializer;
    private final Map<String, Class<?>> eventClasses;

    private MessageSerializer(Gson serializer, Map<String, Class<?>> eventClasses) {
        this.serializer = serializer;
        this.eventClasses = eventClasses;
    }

    private static String getEventTypeFromClass(Class<?> event) {
        return event.getSimpleName();
    }

    public Object parseEvent(String data, String type) {
        Class<?> eventClass = eventClasses.get(type);
        return serializer.fromJson(data, eventClass);
    }

    public String serializeEvent(Object event) {
        return serializer.toJson(event);
    }

    public String getEventType(Object event) {
        return getEventTypeFromClass(event.getClass());
    }

    public String getEventType(Class<?> eventClass) {
        return getEventTypeFromClass(eventClass);
    }

    public final static class Builder {
        private final Map<String, Class<?>> eventClasses = new HashMap<>();
        private Gson serializer = BasicJsonSerializer.getSerializer();

        public MessageSerializer.Builder registerMessage(Class<?> eventClass) {
            String eventType = getEventTypeFromClass(eventClass);
            if (eventClasses.containsKey(eventType)) {
                if (eventClasses.get(eventType).equals(eventClass)) {
                    throw new RuntimeException("Registered same event class twice. Duplicated class: " + eventClass.getName());
                } else {
                    throw new RuntimeException("Two different classes have same name. " +
                            "Remember that name of the old class name can not be changed. " +
                            "Duplicated classes: " + eventClasses.get(eventType).getName() + " & " + eventClass.getName());
                }
            }
            eventClasses.put(eventType, eventClass);
            return this;
        }

        public <T> MessageSerializer.Builder setSerializer(Gson serializer) {
            this.serializer = serializer;
            return this;
        }

        public MessageSerializer build() {
            return new MessageSerializer(serializer, ImmutableMap.copyOf(eventClasses));
        }
    }
}
