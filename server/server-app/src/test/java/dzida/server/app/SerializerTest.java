package dzida.server.app;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterId;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SerializerTest {
    private Serializer serializer = new Serializer();

    @Test
    public void legacyWsMessageIsSerializedToListOfIdAndObject() throws Exception {
        LegacyWsMessage legacyWsMessage = new LegacyWsMessage(1, ImmutableMap.of("test", 5));
        String json = serializer.toJson(legacyWsMessage);
        assertThat(json).isEqualTo("[1,{\"test\":5}]");
    }

    @Test
    public void characterIdIsSerializedToNumber() {
        CharacterId characterId = new CharacterId(4);
        String json = serializer.toJson(characterId);
        assertThat(json).isEqualTo("4");
    }

    @Test
    public void numberCanBeSerializedToCharacterId() {
        String json = "4";
        CharacterId characterId = serializer.fromJson(json, CharacterId.class);
        assertThat(characterId).isEqualTo(new CharacterId(4));
    }

    @Test
    public void entityIdIsSerializedToNumber() {
        Id<String> id = new Id<>(5);
        String json = serializer.toJson(id);
        assertThat(json).isEqualTo("5");
    }

    @Test
    public void entityIdIsDeserializedFromNumber() {
        TypeToken<Id<String>> typeToken = new TypeToken<Id<String>>() {
        };
        Id<String> id = serializer.fromJson("6", typeToken.getType());
        assertThat(id).isEqualTo(new Id<>(6));
    }

    @Test
    public void entityKeyIsSerializedToString() {
        Key<String> key = new Key<>("something");
        String json = serializer.toJson(key);
        assertThat(json).isEqualTo("\"something\"");
    }

    @Test
    public void entityKeyIsDeserializedFromString() {
        TypeToken<Key<String>> typeToken = new TypeToken<Key<String>>() {
        };
        Key<String> key = serializer.fromJson("something", typeToken.getType());
        assertThat(key).isEqualTo(new Key<>("something"));
    }

    @Test
    public void packetIsSerializedToJsonArray() {
        LegacyWsMessage legacyWsMessage1 = new LegacyWsMessage(1, ImmutableMap.of("test", 5));
        LegacyWsMessage legacyWsMessage2 = new LegacyWsMessage(2, ImmutableMap.of("value", 3));
        Packet packet = Packet.builder()
                .addLegacyWsMessage(legacyWsMessage1)
                .addLegacyWsMessage(legacyWsMessage2)
                .build();
        String json = serializer.toJson(packet);
        assertThat(json).isEqualTo("[[[1,{\"test\":5}],[2,{\"value\":3}]]]");

    }
}