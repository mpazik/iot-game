package dzida.server.app.instance;

import dzida.server.app.BasicJsonSerializer;
import dzida.server.app.Container;
import dzida.server.app.TimeSynchroniser;
import dzida.server.app.command.JoinBattleCommand;
import dzida.server.app.command.MoveRawCommand;
import dzida.server.app.command.SkillUseOnCharacterRawCommand;
import dzida.server.app.command.SkillUseOnWorldMapRawCommand;
import dzida.server.app.command.SkillUseOnWorldObjectRawCommand;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.scenario.ScenarioEnd;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;

import static dzida.server.app.BasicJsonSerializer.keyTypeAdapter;

public class InstanceSerializer {

    public static JsonProtocol createSerializer() {
        return new JsonProtocol.Builder()
                .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
                .registerParsingMessageType(2, MoveRawCommand.class)
                .registerParsingMessageType(3, SkillUseOnCharacterRawCommand.class)
                .registerParsingMessageType(4, SkillUseOnWorldMapRawCommand.class)
                .registerParsingMessageType(6, TimeSynchroniser.TimeSyncRequest.class)
                .registerParsingMessageType(7, JoinBattleCommand.class)
                .registerParsingMessageType(8, BackdoorCommandResolver.BackdoorCommand.class)
                .registerParsingMessageType(11, SkillUseOnWorldObjectRawCommand.class)
                .registerSerializationMessageType(5, CharacterSpawned.class)
                .registerSerializationMessageType(6, CharacterDied.class)
                .registerSerializationMessageType(7, CharacterMoved.class)
                .registerSerializationMessageType(8, SkillUsedOnCharacter.class)
                .registerSerializationMessageType(9, CharacterGotDamage.class)
                .registerSerializationMessageType(11, StateSynchroniser.InitialMessage.class)
                .registerSerializationMessageType(12, ServerMessage.class)
                .registerSerializationMessageType(16, TimeSynchroniser.TimeSyncResponse.class)
                .registerSerializationMessageType(17, Container.JoinToInstance.class)
                .registerSerializationMessageType(19, ScenarioEnd.class)
                .registerSerializationMessageType(21, SkillUsedOnWorldMap.class)
                .registerSerializationMessageType(22, WorldObjectCreated.class)
                .registerSerializationMessageType(23, SkillUsedOnWorldObject.class)
                .registerSerializationMessageType(24, WorldObjectRemoved.class)
                .build();
    }
}
