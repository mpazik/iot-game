package dzida.server.app.store;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import dzida.server.app.BasicJsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class EventSerializer {
    private final Gson serializer;
    private final Map<String, Class<?>> eventClasses;

    private EventSerializer(Gson serializer, Map<String, Class<?>> eventClasses) {
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

        public EventSerializer.Builder registerEvent(Class<?> eventClass) {
            String eventType = getEventTypeFromClass(eventClass);
            eventClasses.put(eventType, eventClass);
            return this;
        }

        public <T> EventSerializer.Builder setSerializer(Gson serializer) {
            this.serializer = serializer;
            return this;
        }

        public EventSerializer build() {
            return new EventSerializer(serializer, ImmutableMap.copyOf(eventClasses));
        }
    }
}
