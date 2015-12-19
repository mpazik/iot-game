package dzida.server.app;

import com.google.common.collect.ImmutableMap;
import dzida.server.core.character.CharacterId;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SerializerTest {

    @Test
    public void packetIsSerializedToListOfIdAndObject() throws Exception {
        Packet packet = new Packet(1, ImmutableMap.of("test", 5));
        String json = Serializer.getSerializer().toJson(packet);
        assertThat(json).isEqualTo("[1,{\"test\":5}]");
    }

    @Test
    public void characterIdIsSerializedToNumber() {
        CharacterId characterId = new CharacterId(4);
        String json = Serializer.getSerializer().toJson(characterId);
        assertThat(json).isEqualTo("4");
    }

    @Test
    public void numberCanBeSerializedToCharacterId() {
        String json = "4";
        CharacterId characterId = Serializer.getSerializer().fromJson(json, CharacterId.class);
        assertThat(characterId).isEqualTo(new CharacterId(4));
    }
}