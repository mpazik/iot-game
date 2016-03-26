package dzida.server.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterId;
import dzida.server.core.player.Player;

import java.io.IOException;
import java.lang.reflect.Type;

public final class Serializer {
    private final TypeAdapter<CharacterId> characterIdTypeAdapter = new TypeAdapter<CharacterId>() {
        @Override
        public void write(JsonWriter out, CharacterId characterId) throws IOException {
            out.value(characterId.getValue());
        }

        @Override
        public CharacterId read(JsonReader in) throws IOException {
            int id = in.nextInt();
            return new CharacterId(id);
        }
    };

    private final TypeAdapter<Id<?>> idTypeAdapter = new TypeAdapter<Id<?>>() {
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

    private final TypeAdapter<Key<?>> keyTypeAdapter = new TypeAdapter<Key<?>>() {
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

    private final TypeAdapter<Player.Id> playerIdTypeAdapter = new TypeAdapter<Player.Id>() {
        @Override
        public void write(JsonWriter out, Player.Id characterId) throws IOException {
            out.value(characterId.getValue());
        }

        @Override
        public Player.Id read(JsonReader in) throws IOException {
            int id = in.nextInt();
            return new Player.Id(id);
        }
    };

    private final TypeAdapter<Packet> packetTypeAdapter = new TypeAdapter<Packet>() {

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

    private final Gson gsonForPacket = new GsonBuilder()
            .registerTypeAdapter(CharacterId.class, characterIdTypeAdapter)
            .registerTypeAdapter(Player.Id.class, playerIdTypeAdapter)
            .registerTypeHierarchyAdapter(Id.class, idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
            .create();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CharacterId.class, characterIdTypeAdapter)
            .registerTypeAdapter(Player.Id.class, playerIdTypeAdapter)
            .registerTypeHierarchyAdapter(Id.class, idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)

            .registerTypeAdapter(Packet.class, packetTypeAdapter)
            .create();


    public String toJson(Object data) {
        return gson.toJson(data);
    }

    public <T> T fromJson(JsonElement data, Class<T> classOfT) {
        return gson.fromJson(data, classOfT);
    }

    public <T> T fromJson(String data, Type type) {
        return gson.fromJson(data, type);
    }

    public <T> T fromJson(JsonReader reader, Type type) {
        return gson.fromJson(reader, type);
    }
}
