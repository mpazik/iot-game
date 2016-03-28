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
import dzida.server.core.entity.EntityChangesWithType;
import dzida.server.core.entity.EntityId;
import dzida.server.core.entity.EntityType;
import dzida.server.core.player.Player;

import java.io.IOException;
import java.lang.reflect.Type;

public final class Serializer {
    private final TypeAdapter<CharacterId> characterIdAdapter = new TypeAdapter<CharacterId>() {
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

    private final TypeAdapter<Id<?>> idAdapter = new TypeAdapter<Id<?>>() {
        @Override
        public void write(JsonWriter out, Id<?> id) throws IOException {
            out.value(id.getValue());
        }

        @Override
        public Id<?> read(JsonReader in) throws IOException {
            long id = in.nextLong();
            return new Id<>(id);
        }
    };

    private final TypeAdapter<EntityId> entityIdAdapter = new TypeAdapter<EntityId>() {
        @Override
        public void write(JsonWriter out, EntityId entityId) throws IOException {
            out.value(entityId.getValue());
        }

        @Override
        public EntityId read(JsonReader in) throws IOException {
            long id = in.nextLong();
            return new EntityId(id);
        }
    };

    private final TypeAdapter<EntityType> entityTypeAdapter = new TypeAdapter<EntityType>() {
        @Override
        public void write(JsonWriter out, EntityType entityType) throws IOException {
            out.value(entityType.getValue());
        }

        @Override
        public EntityType read(JsonReader in) throws IOException {
            int id = in.nextInt();
            return new EntityType(id);
        }
    };

    private final TypeAdapter<Key<?>> keyAdapter = new TypeAdapter<Key<?>>() {
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

    private final TypeAdapter<Player.Id> playerIdAdapter = new TypeAdapter<Player.Id>() {
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

    private final TypeAdapter<LegacyWsMessage> legacyWsMessageTypeAdapter = new TypeAdapter<LegacyWsMessage>() {

        @Override
        public void write(JsonWriter out, LegacyWsMessage legacyWsMessage) throws IOException {
            out.beginArray()
                    .value(legacyWsMessage.getType())
                    .jsonValue(elementalGson.toJson(legacyWsMessage.getData()))
                    .endArray();
        }

        @Override
        public LegacyWsMessage read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    private final TypeAdapter<EntityChangesWithType> entityChangesWithTypeAdapter = new TypeAdapter<EntityChangesWithType>() {

        @Override
        public void write(JsonWriter out, EntityChangesWithType changes) throws IOException {
            out.beginArray()
                    .jsonValue(elementalGson.toJson(changes.entityId))
                    .jsonValue(elementalGson.toJson(changes.entityType))
                    .jsonValue(elementalGson.toJson(changes.changes))
                    .endArray();
        }

        @Override
        public EntityChangesWithType read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
    };


    private final TypeAdapter<Packet> packetAdapter = new TypeAdapter<Packet>() {

        @Override
        public void write(JsonWriter out, Packet packet) throws IOException {
            out.beginArray()
                    .jsonValue(packetGson.toJson(packet.getLegacyWsMessages()))
                    .jsonValue(packetGson.toJson(packet.getChanges()))
                    .endArray();
        }

        @Override
        public Packet read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    private final Gson elementalGson = new GsonBuilder()
            .registerTypeAdapter(CharacterId.class, characterIdAdapter)
            .registerTypeAdapter(Player.Id.class, playerIdAdapter)
            .registerTypeHierarchyAdapter(Id.class, idAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyAdapter)
            .registerTypeHierarchyAdapter(EntityId.class, entityIdAdapter)
            .registerTypeHierarchyAdapter(EntityType.class, entityTypeAdapter)
            .create();

    private final Gson packetGson = new GsonBuilder()
            .registerTypeAdapter(LegacyWsMessage.class, legacyWsMessageTypeAdapter)
            .registerTypeAdapter(EntityChangesWithType.class, entityChangesWithTypeAdapter)
            .create();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CharacterId.class, characterIdAdapter)
            .registerTypeAdapter(Player.Id.class, playerIdAdapter)
            .registerTypeHierarchyAdapter(Id.class, idAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyAdapter)
            .registerTypeAdapter(Packet.class, packetAdapter)
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
