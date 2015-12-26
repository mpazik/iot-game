package dzida.server.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.core.character.CharacterId;
import dzida.server.core.player.Player;

import java.io.IOException;

public final class Serializer {
    private static final TypeAdapter<CharacterId> characterIdTypeAdapter = new TypeAdapter<CharacterId>() {
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
    private static final TypeAdapter<Player.Id> playerIdTypeAdapter = new TypeAdapter<Player.Id>() {
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

    private static final TypeAdapter<Packet> packetTypeAdapter = new TypeAdapter<Packet>() {
        private final Gson gsonWithIdAdaptersPackets = new GsonBuilder()
                .registerTypeAdapter(CharacterId.class, characterIdTypeAdapter)
                .registerTypeAdapter(Player.Id.class, playerIdTypeAdapter)
                .create();

        @Override
        public void write(JsonWriter out, Packet packet) throws IOException {
            out.beginArray()
                    .value(packet.getType())
                    .jsonValue(gsonWithIdAdaptersPackets.toJson(packet.getData()))
                    .endArray();
        }

        @Override
        public Packet read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CharacterId.class, characterIdTypeAdapter)
            .registerTypeAdapter(Player.Id.class, playerIdTypeAdapter)
            .registerTypeAdapter(Packet.class, packetTypeAdapter)
            .create();

    private Serializer() {
        //no instance
    }

    public static Gson getSerializer() {
        return gson;
    }
}
