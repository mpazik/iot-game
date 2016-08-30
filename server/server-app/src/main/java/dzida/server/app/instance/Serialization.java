package dzida.server.app.instance;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import dzida.server.app.command.EatAppleRawCommand;
import dzida.server.app.command.MoveRawCommand;
import dzida.server.app.command.SkillUseOnCharacterRawCommand;
import dzida.server.app.command.SkillUseOnWorldMapRawCommand;
import dzida.server.app.command.SkillUseOnWorldObjectRawCommand;
import dzida.server.app.instance.scenario.ScenarioEvent;
import dzida.server.app.map.descriptor.OpenWorld;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.app.serialization.BasicJsonSerializer;
import dzida.server.app.serialization.MessageSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.scenario.ScenarioEnd;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.CharacterHealed;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;

import static dzida.server.app.serialization.BasicJsonSerializer.keyTypeAdapter;

public class Serialization {

    private Serialization() {
        // no op
    }

    public static JsonProtocol createSerializer() {
        return new JsonProtocol.Builder()
                .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
                .registerParsingMessageType(2, MoveRawCommand.class)
                .registerParsingMessageType(3, SkillUseOnCharacterRawCommand.class)
                .registerParsingMessageType(4, SkillUseOnWorldMapRawCommand.class)
                .registerParsingMessageType(8, BackdoorCommandResolver.BackdoorCommand.class)
                .registerParsingMessageType(11, SkillUseOnWorldObjectRawCommand.class)
                .registerParsingMessageType(12, EatAppleRawCommand.class)
                .registerSerializationMessageType(5, CharacterSpawned.class)
                .registerSerializationMessageType(6, CharacterDied.class)
                .registerSerializationMessageType(7, CharacterMoved.class)
                .registerSerializationMessageType(8, SkillUsedOnCharacter.class)
                .registerSerializationMessageType(9, CharacterGotDamage.class)
                .registerSerializationMessageType(11, StateSynchroniser.InitialMessage.class)
                .registerSerializationMessageType(12, ServerMessage.class)
                .registerSerializationMessageType(19, ScenarioEnd.class)
                .registerSerializationMessageType(21, SkillUsedOnWorldMap.class)
                .registerSerializationMessageType(22, WorldObjectCreated.class)
                .registerSerializationMessageType(23, SkillUsedOnWorldObject.class)
                .registerSerializationMessageType(24, WorldObjectRemoved.class)
                .registerSerializationMessageType(25, Instance.UserCharacterMessage.class)
                .registerSerializationMessageType(26, CharacterHealed.class)
                .build();
    }

    public static MessageSerializer createInstanceEventSerializer() {
        return new MessageSerializer.Builder()
                .registerMessage(CharacterSpawned.class)
                .registerMessage(CharacterDied.class)
                .registerMessage(CharacterMoved.class)
                .registerMessage(SkillUsedOnCharacter.class)
                .registerMessage(CharacterGotDamage.class)
                .registerMessage(StateSynchroniser.InitialMessage.class)
                .registerMessage(ServerMessage.class)
                .registerMessage(ScenarioEnd.class)
                .registerMessage(SkillUsedOnWorldMap.class)
                .registerMessage(WorldObjectCreated.class)
                .registerMessage(SkillUsedOnWorldObject.class)
                .registerMessage(WorldObjectRemoved.class)
                .registerMessage(Instance.UserCharacterMessage.class)
                .registerMessage(CharacterHealed.class)
                .build();
    }

    public static MessageSerializer createScenarioEventSerializer() {
        return new MessageSerializer.Builder()
                .setSerializer(new GsonBuilder()
                        .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                        .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
                        .registerTypeAdapter(Scenario.class, (JsonDeserializer<Scenario>) (json, typeOfT, context) -> {
                            JsonObject scenario = json.getAsJsonObject();
                            String type = scenario.get("type").getAsString();
                            switch (type) {
                                case "open-world":
                                    return BasicJsonSerializer.getSerializer().fromJson(scenario, OpenWorld.class);
                                case "survival":
                                    return BasicJsonSerializer.getSerializer().fromJson(scenario, Survival.class);
                                default:
                                    throw new RuntimeException("can not parse scenario of type: " + type);
                            }
                        })
                        .registerTypeAdapter(Scenario.class, (JsonSerializer<Scenario>) (src, typeOfSrc, context1) ->
                                BasicJsonSerializer.getSerializer().toJsonTree(src))
                        .create())
                .registerMessage(ScenarioEvent.ScenarioStarted.class)
                .registerMessage(ScenarioEvent.ScenarioFinished.class)
                .build();
    }
}
