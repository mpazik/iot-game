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
    public void packetIsSerializedToListOfIdAndObject() throws Exception {
        Packet packet = new Packet(1, ImmutableMap.of("test", 5));
        String json = serializer.toJson(packet);
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
    public void EntityIdIsSerializedToNumber() {
        Id<String> id = new Id<>(5);
        String json = serializer.toJson(id);
        assertThat(json).isEqualTo("5");
    }

    @Test
    public void EntityIdIsDeserializedFromNumber() {
        TypeToken<Id<String>> typeToken = new TypeToken<Id<String>>() {
        };
        Id<String> id = serializer.fromJson("6", typeToken.getType());
        assertThat(id).isEqualTo(new Id<>(6));
    }

    @Test
    public void EntityKeyIsSerializedToString() {
        Key<String> key = new Key<>("something");
        String json = serializer.toJson(key);
        assertThat(json).isEqualTo("\"something\"");
    }

    @Test
    public void EntityKeyIsDeserializedFromString() {
        TypeToken<Key<String>> typeToken = new TypeToken<Key<String>>() {
        };
        Key<String> key = serializer.fromJson("something", typeToken.getType());
        assertThat(key).isEqualTo(new Key<>("something"));
    }
}