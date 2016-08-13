package dzida.server.app;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dzida.server.app.command.Command;
import dzida.server.app.command.JoinBattleCommand;
import dzida.server.app.command.MoveCommand;
import dzida.server.app.command.SendMessageCommand;
import dzida.server.app.command.SkillUseOnCharacterCommand;
import dzida.server.app.command.SkillUseOnWorldMapCommand;
import dzida.server.app.command.SkillUseOnWorldObjectCommand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CommandParser {
    // commands
    private static final int Move = 2;
    private static final int UseSkillOnCharacter = 3;
    private static final int UseSkillOnWorldMap = 4;
    private static final int JoinBattle = 7;
    private static final int SendMessage = 10;
    private static final int UseSkillOnWorldObject = 11;

    // requests
    private static final int TimeSync = 6;
    private static final int Backdoor = 8;

    private final Gson serializer = Serializer.getSerializer();

    public CommandParser() {
    }

    public List<Command> readPacket(String payload) {
        try {
            JsonArray messages = new Gson().fromJson(payload, JsonArray.class);
            Stream<JsonElement> stream = StreamSupport.stream(messages.spliterator(), false);
            return stream.map(element -> {
                JsonArray message = element.getAsJsonArray();
                int type = message.get(0).getAsNumber().intValue();
                JsonElement data = message.get(1);
                return readCommand(type, data);
            }).collect(Collectors.toList());
        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
            System.out.println(Throwables.getStackTraceAsString(e));
            return Collections.emptyList();
        }
    }

    private Command readCommand(int type, JsonElement data) {
        switch (type) {
            case Move:
                return serializer.fromJson(data, MoveCommand.class);
            case UseSkillOnCharacter:
                return serializer.fromJson(data, SkillUseOnCharacterCommand.class);
            case UseSkillOnWorldMap:
                return serializer.fromJson(data, SkillUseOnWorldMapCommand.class);
            case UseSkillOnWorldObject:
                return serializer.fromJson(data, SkillUseOnWorldObjectCommand.class);
            case TimeSync:
                return serializer.fromJson(data, TimeSynchroniser.TimeSyncRequest.class);
            case JoinBattle:
                return serializer.fromJson(data, JoinBattleCommand.class);
            case Backdoor:
                return serializer.fromJson(data, BackdoorCommandResolver.BackdoorCommand.class);
            case SendMessage:
                return serializer.fromJson(data, SendMessageCommand.class);
            default:
                throw new RuntimeException("Unrecognized command type");
        }
    }

}


