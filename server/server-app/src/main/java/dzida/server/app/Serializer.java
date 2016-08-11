package dzida.server.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

import java.io.IOException;

public final class Serializer {
    private static final TypeAdapter<Id<?>> idTypeAdapter = new TypeAdapter<Id<?>>() {
        @Override
        public void write(JsonWriter out, Id<?> characterId) throws IOException {
            out.value(characterId.getValue());
        }

        @Override
        public Id<?> read(JsonReader in) throws IOException {
            long id = in.nextLong();
            return new Id<>(id);
        }
    };

    private static final TypeAdapter<Key<?>> keyTypeAdapter = new TypeAdapter<Key<?>>() {
        @Override
        public void write(JsonWriter out, Key<?> key) throws IOException {
            out.value(key.getValue());
        }

        @Override
        public Key<?> read(JsonReader in) throws IOException {
            String key = in.nextString();
            return new Key<>(key);
        }
    };

    private static final TypeAdapter<Packet> packetTypeAdapter = new TypeAdapter<Packet>() {

        @Override
        public void write(JsonWriter out, Packet packet) throws IOException {
            out.beginArray()
                    .value(packet.getType())
                    .jsonValue(gsonForPacket.toJson(packet.getData()))
                    .endArray();
        }

        @Override
        public Packet read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    private static final Gson gsonForPacket = new GsonBuilder()
            .registerTypeHierarchyAdapter(Id.class, idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
            .create();

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Id.class, idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
            .registerTypeAdapter(Packet.class, packetTypeAdapter)
            .create();

    private Serializer() {
        //no instance
    }

    public static Gson getSerializer() {
        return gson;
    }
}
