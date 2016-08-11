package dzida.server.app;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.model.Character;
import dzida.server.core.chat.ChatService;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillCommandHandler;
import dzida.server.core.world.object.WorldObject;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CommandResolver {
    // commands
    private static final int Move = 2;
    private static final int UseSkillOnCharacter = 3;
    private static final int UseSkillOnWorldMap = 4;
    private static final int JoinBattle = 7;
    private static final int GoToHome = 9;
    private static final int SendMessage = 10;
    private static final int UseSkillOnWorldObject = 11;

    // requests
    private static final int PlayingPlayer = 5;
    private static final int TimeSync = 6;
    private static final int Backdoor = 8;

    private final Gson serializer = Serializer.getSerializer();
    private final PositionCommandHandler positionCommandHandler;
    private final TimeSynchroniser timeSynchroniser;
    private final SkillCommandHandler skillCommandHandler;
    private final CharacterCommandHandler characterCommandHandler;
    private final Arbiter arbiter;
    private final BackdoorCommandResolver backdoorCommandResolver;
    private final PlayerService playerService;
    private final ChatService chatService;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler,
            TimeSynchroniser timeSynchroniser,
            Arbiter arbiter, PlayerService playerService, ChatService chatService) {
        this.positionCommandHandler = positionCommandHandler;
        this.timeSynchroniser = timeSynchroniser;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;
        this.arbiter = arbiter;
        this.playerService = playerService;
        this.chatService = chatService;

        if (Configuration.isDevMode()) {
            backdoorCommandResolver = new BackdoorCommandResolver(serializer);
        } else {
            backdoorCommandResolver = BackdoorCommandResolver.NoOpResolver;
        }
    }

    public List<GameEvent> createCharacter(Character character) {
        return characterCommandHandler.spawnCharacter(character);
    }

    public List<GameEvent> removeCharacter(CharacterId characterId) {
        return characterCommandHandler.killCharacter(characterId);
    }

    public List<GameEvent> dispatchPacket(Id<Player> playerId, CharacterId characterId, String payload, Consumer<GameEvent> send) {
        try {
            JsonArray messages = new Gson().fromJson(payload, JsonArray.class);
            Stream<JsonElement> stream = StreamSupport.stream(((Iterable<JsonElement>) messages::iterator).spliterator(), false);
            return stream.flatMap(element -> {
                JsonArray message = element.getAsJsonArray();
                int type = message.get(0).getAsNumber().intValue();
                JsonElement data = message.get(1);
                return dispatchMessage(playerId, characterId, type, data, send).stream();
            }).collect(Collectors.toList());
        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
            System.out.println(Throwables.getStackTraceAsString(e));
            send.accept(ServerMessage.error("can not parse JSON"));
            return Collections.emptyList();
        }
    }

    private List<GameEvent> dispatchMessage(Id<Player> playerId, CharacterId characterId, int type, JsonElement data, Consumer<GameEvent> send) {
        switch (type) {
            case Move:
                return positionCommandHandler.move(characterId, serializer.fromJson(data, Point.class), PositionService.PlayerSpeed);
            case UseSkillOnCharacter:
                SkillUseOnCharacter skillUseOnCharacter = serializer.fromJson(data, SkillUseOnCharacter.class);
                return skillCommandHandler.useSkillOnCharacter(characterId, skillUseOnCharacter.skillId, skillUseOnCharacter.target);
            case UseSkillOnWorldMap:
                SkillUseOnWorldMap skillUseOnWorldMap = serializer.fromJson(data, SkillUseOnWorldMap.class);
                return skillCommandHandler.useSkillOnWorldMap(characterId, skillUseOnWorldMap.skillId, skillUseOnWorldMap.x, skillUseOnWorldMap.y);
            case UseSkillOnWorldObject:
                SkillUseOnWorldObject skillUseOnWorldObject = serializer.fromJson(data, SkillUseOnWorldObject.class);
                return skillCommandHandler.useSkillOnWorldObject(characterId, skillUseOnWorldObject.skillId, skillUseOnWorldObject.target);
            case PlayingPlayer:
                return Collections.emptyList();
            case TimeSync:
                TimeSynchroniser.TimeSyncRequest timeSyncRequest = serializer.fromJson(data, TimeSynchroniser.TimeSyncRequest.class);
                TimeSynchroniser.TimeSyncResponse timeSyncResponse = timeSynchroniser.timeSync(timeSyncRequest);
                send.accept(timeSyncResponse);
                return Collections.emptyList();
            case JoinBattle:
                String map = data.getAsJsonObject().get("map").getAsString();
                int difficultyLevel = data.getAsJsonObject().get("difficultyLevel").getAsInt();
                Player.Data playerData = playerService.getPlayer(playerId).getData();
                Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), difficultyLevel);
                playerService.updatePlayerData(playerId, updatedPlayerData);
                String instanceKey = map + new Random().nextInt();
                arbiter.startInstance(instanceKey, map, address -> send.accept(new JoinToInstance(address.toString())), difficultyLevel);
                return Collections.emptyList();
            case Backdoor:
                return backdoorCommandResolver.resolveCommand(characterId, data, send);
            case GoToHome:
                send.accept(new JoinToInstance(arbiter.getHomeInstnceAddress().toString()));
                return Collections.emptyList();
            case SendMessage:
                String message = data.getAsJsonObject().get("message").getAsString();
                return chatService.handleMessage(playerId, message);
            default:
                return Collections.emptyList();
        }
    }

    private static class SkillUseOnCharacter {
        Id<Skill> skillId;
        CharacterId target;
    }

    private static class SkillUseOnWorldMap {
        Id<Skill> skillId;
        double x;
        double y;
    }

    private static class SkillUseOnWorldObject {
        final Id<Skill> skillId;
        final Id<WorldObject> target;

        SkillUseOnWorldObject(Id<Skill> skillId, Id<WorldObject> target) {
            this.skillId = skillId;
            this.target = target;
        }
    }

    private static class JoinToInstance implements GameEvent {
        final String address;

        JoinToInstance(String address) {
            this.address = address;
        }

        @Override
        public int getId() {
            return InstanceCreated;
        }
    }
}