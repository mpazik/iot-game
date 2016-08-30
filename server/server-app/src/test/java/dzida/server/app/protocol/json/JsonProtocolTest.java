package dzida.server.app.protocol.json;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonProtocolTest {
    private static final TypeAdapter<Key> keyTypeAdapter = new TypeAdapter<Key>() {
        @Override
        public void write(JsonWriter out, Key key) throws IOException {
            out.value(key.value);
        }

        @Override
        public Key read(JsonReader in) throws IOException {
            String key = in.nextString();
            return new Key<>(key);
        }
    };

    private static final TypeAdapter<Bar> barTypeAdapter = new TypeAdapter<Bar>() {

        @Override
        public void write(JsonWriter out, Bar bar) throws IOException {
            out.beginArray().value(bar.value).endArray();
        }

        @Override
        public Bar read(JsonReader in) throws IOException {
            in.beginArray();
            String value = in.nextString();
            in.endArray();
            return new Bar(value);
        }
    };

    private static final TypeAdapter<CustomSerializationMessage> CustomSerializationMessageTypeAdapter = new TypeAdapter<CustomSerializationMessage>() {

        @Override
        public void write(JsonWriter out, CustomSerializationMessage customSerializationMessage) throws IOException {
            out.beginArray().value(customSerializationMessage.text).value(customSerializationMessage.number).endArray();
        }

        @Override
        public CustomSerializationMessage read(JsonReader in) throws IOException {
            in.beginArray();
            String text = in.nextString();
            int number = in.nextInt();
            in.endArray();
            return new CustomSerializationMessage(text, number);
        }
    };

    final String trivialMessageType = "\"TrivialMessage\"";
    final String nestedObjectMessageType = "\"NestedObjectMessage\"";
    final String customSerializationObjectMessageType = "\"CustomSerializationObjectMessage\"";
    final String customSerializationMessageType = "\"CustomSerializationMessage\"";
    final String customSerializationGenericObjectMessageType = "\"CustomSerializationGenericObjectMessage\"";
    final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Bar.class, barTypeAdapter)
            .registerTypeAdapter(CustomSerializationMessage.class, CustomSerializationMessageTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
            .create();
    final ImmutableSet<Class<?>> messages = ImmutableSet.of(
            TrivialMessage.class,
            NestedObjectMessage.class,
            CustomSerializationObjectMessage.class,
            CustomSerializationMessage.class,
            CustomSerializationGenericObjectMessage.class
    );

    JsonProtocol serializer = JsonProtocol.create(gson, messages, messages);

    @Test
    public void serializing_ifMessageIsRegistered_returnsProperString() {
        String data = serializer.serializeMessage(new TrivialMessage("some text"));
        assertThat(data).isEqualTo("[" + trivialMessageType + ",{\"text\":\"some text\"}]");
    }

    @Test
    public void parsing_ifMessageIsRegistered_returnsMessageObject() {
        Object message = serializer.parseMessage("[" + trivialMessageType + ",{\"text\":\"some text\"}]");
        assertThat(message).isEqualTo(new TrivialMessage("some text"));
    }

    @Test
    public void serializing_ifMessageIsUnRegistered_returnsNull() {
        String data = serializer.serializeMessage(new UnregisteredMessage("test"));
        assertThat(data).isNull();
    }

    @Test
    public void parsing_ifMessageIsUnRegistered_returnsNull() {
        Object message = serializer.parseMessage("[" + 1234 + ",{\"text\":\"some text\"}]");
        assertThat(message).isNull();
    }

    @Test
    public void serializing_ifMessageHasNestedObject() {
        String data = serializer.serializeMessage(new NestedObjectMessage(new Foo("some name")));
        assertThat(data).isEqualTo("[" + nestedObjectMessageType + ",{\"foo\":{\"name\":\"some name\"}}]");
    }

    @Test
    public void parsing_iifMessageHasNestedObject() {
        Object message = serializer.parseMessage("[" + nestedObjectMessageType + ",{\"foo\":{\"name\":\"some name\"}}]");
        assertThat(message).isEqualTo(new NestedObjectMessage(new Foo("some name")));
    }

    @Test
    public void serializing_ifTypeAdapterIsRegistered_worksForValueType() {
        String data = serializer.serializeMessage(new CustomSerializationObjectMessage(new Bar("text")));
        assertThat(data).isEqualTo("[" + customSerializationObjectMessageType + ",{\"bar\":[\"text\"]}]");
    }

    @Test
    public void parsing_ifTypeAdapterIsRegistered_worksForValueType() {
        Object message = serializer.parseMessage("[" + customSerializationObjectMessageType + ",{\"bar\":[\"text\"]}]");
        assertThat(message).isEqualTo(new CustomSerializationObjectMessage(new Bar("text")));
    }

    @Test
    public void serializing_ifTypeAdapterIsRegistered_worksForMessageType() {
        String data = serializer.serializeMessage(new CustomSerializationMessage("text", 4));
        assertThat(data).isEqualTo("[" + customSerializationMessageType + ",[\"text\",4]]");
    }

    @Test
    public void parsing_ifTypeAdapterIsRegistered_worksForMessageType() {
        Object message = serializer.parseMessage("[" + customSerializationMessageType + ",[\"text\",4]]");
        assertThat(message).isEqualTo(new CustomSerializationMessage("text", 4));
    }

    @Test
    public void serializing_ifGenericTypeAdapterIsRegistered_worksForGenericValueType() {
        String data = serializer.serializeMessage(new CustomSerializationGenericObjectMessage(new Key<>("keyValue")));
        assertThat(data).isEqualTo("[" + customSerializationGenericObjectMessageType + ",{\"key\":\"keyValue\"}]");
    }

    @Test
    public void parsing_ifGenericTypeAdapterIsRegistered_worksForGenericValueType() {
        Object message = serializer.parseMessage("[" + customSerializationGenericObjectMessageType + ",{\"key\":\"keyValue\"}]");
        assertThat(message).isEqualTo(new CustomSerializationGenericObjectMessage(new Key<>("keyValue")));
    }

    private final static class TrivialMessage {
        final String text;

        public TrivialMessage(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrivialMessage that = (TrivialMessage) o;
            return Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }
    }

    private final static class UnregisteredMessage {
        final String text;

        public UnregisteredMessage(String text) {
            this.text = text;
        }
    }

    private final static class NestedObjectMessage {
        final Foo foo;

        private NestedObjectMessage(Foo foo) {
            this.foo = foo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NestedObjectMessage that = (NestedObjectMessage) o;
            return Objects.equals(foo, that.foo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(foo);
        }
    }

    private final static class CustomSerializationObjectMessage {
        final Bar bar;

        private CustomSerializationObjectMessage(Bar bar) {
            this.bar = bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomSerializationObjectMessage that = (CustomSerializationObjectMessage) o;
            return Objects.equals(bar, that.bar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bar);
        }
    }

    private final static class CustomSerializationMessage {
        final String text;
        final int number;

        public CustomSerializationMessage(String text, int number) {
            this.text = text;
            this.number = number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomSerializationMessage that = (CustomSerializationMessage) o;
            return number == that.number &&
                    Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, number);
        }
    }


    private final static class CustomSerializationGenericObjectMessage {
        final Key<Foo> key;

        public CustomSerializationGenericObjectMessage(Key<Foo> key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomSerializationGenericObjectMessage that = (CustomSerializationGenericObjectMessage) o;
            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }


    private final static class Foo {
        final String name;

        public Foo(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Foo foo = (Foo) o;
            return Objects.equals(name, foo.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private final static class Bar {
        final String value;

        public Bar(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bar bar = (Bar) o;
            return Objects.equals(value, bar.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    @SuppressWarnings("unused")
    private final static class Key<T> {
        final String value;

        public Key(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key<?> key = (Key<?>) o;
            return Objects.equals(value, key.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}