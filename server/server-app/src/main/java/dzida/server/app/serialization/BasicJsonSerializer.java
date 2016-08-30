package dzida.server.app.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

import java.io.IOException;

public final class BasicJsonSerializer {
    public static final TypeAdapter<Id> idTypeAdapter = new TypeAdapter<Id>() {
        @Override
        public void write(JsonWriter out, Id characterId) throws IOException {
            out.value(characterId.getValue());
        }

        @Override
        public Id read(JsonReader in) throws IOException {
            long id = in.nextLong();
            return new Id<>(id);
        }
    };

    public static final TypeAdapter<Key> keyTypeAdapter = new TypeAdapter<Key>() {
        @Override
        public void write(JsonWriter out, Key key) throws IOException {
            out.value(key.getValue());
        }

        @Override
        public Key read(JsonReader in) throws IOException {
            String key = in.nextString();
            return new Key<>(key);
        }
    };

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Id.class, idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
            .create();

    private BasicJsonSerializer() {
        //no instance
    }

    public static Gson getSerializer() {
        return gson;
    }
}
