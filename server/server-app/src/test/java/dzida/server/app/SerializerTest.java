package dzida.server.app;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterId;
import org.junit.Test;

import static dzida.server.app.Serializer.getSerializer;
import static org.assertj.core.api.Assertions.assertThat;


public class SerializerTest {

    @Test
    public void packetIsSerializedToListOfIdAndObject() throws Exception {
        Packet packet = new Packet(1, ImmutableMap.of("test", 5));
        String json = getSerializer().toJson(packet);
        assertThat(json).isEqualTo("[1,{\"test\":5}]");
    }

    @Test
    public void characterIdIsSerializedToNumber() {
        CharacterId characterId = new CharacterId(4);
        String json = getSerializer().toJson(characterId);
        assertThat(json).isEqualTo("4");
    }

    @Test
    public void numberCanBeSerializedToCharacterId() {
        String json = "4";
        CharacterId characterId = getSerializer().fromJson(json, CharacterId.class);
        assertThat(characterId).isEqualTo(new CharacterId(4));
    }

    @Test
    public void EntityIdIsSerializedToNumber() {
        Id<String> id = new Id<>(5);
        String json = getSerializer().toJson(id);
        assertThat(json).isEqualTo("5");
    }

    @Test
    public void EntityIdIsDeserializedFromNumber() {
        TypeToken<Id<String>> typeToken = new TypeToken<Id<String>>() {
        };
        Id<String> id = getSerializer().fromJson("6", typeToken.getType());
        assertThat(id).isEqualTo(new Id<>(6));
    }

    @Test
    public void EntityKeyIsSerializedToString() {
        Key<String> key = new Key<>("something");
        String json = getSerializer().toJson(key);
        assertThat(json).isEqualTo("\"something\"");
    }

    @Test
    public void EntityKeyIsDeserializedFromString() {
        TypeToken<Key<String>> typeToken = new TypeToken<Key<String>>() {
        };
        Key<String> key = getSerializer().fromJson("something", typeToken.getType());
        assertThat(key).isEqualTo(new Key<>("something"));
    }
}