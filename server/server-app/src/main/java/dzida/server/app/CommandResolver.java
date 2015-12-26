package dzida.server.app;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Position;
import dzida.server.core.skill.SkillCommandHandler;
import lombok.Value;

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
    private static final int UseSkill = 3;
    private static final int JoinBattle = 7;
    private static final int GoToHome = 9;

    // requests
    private static final int Ping = 4;
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
    private final CharacterService characterService;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler,
            TimeSynchroniser timeSynchroniser,
            Arbiter arbiter, PlayerService playerService,
            CharacterService characterService) {
        this.positionCommandHandler = positionCommandHandler;
        this.timeSynchroniser = timeSynchroniser;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;
        this.arbiter = arbiter;
        this.playerService = playerService;
        this.characterService = characterService;

        if (Configuration.isDevMode()) {
            backdoorCommandResolver = new BackdoorCommandResolver(serializer);
        } else {
            backdoorCommandResolver = BackdoorCommandResolver.NoOpResolver;
        }
    }

    public List<GameEvent> createCharacter(Character character) {
        return characterCommandHandler.addCharacter(character);
    }

    public List<GameEvent> removeCharacter(CharacterId characterId) {
        return characterCommandHandler.removeCharacter(characterId);
    }

    public List<GameEvent> dispatchPacket(CharacterId characterId, String payload, Consumer<GameEvent> send) {
        try {
            JsonArray messages = new Gson().fromJson(payload, JsonArray.class);
            Stream<JsonElement> stream = StreamSupport.stream(((Iterable<JsonElement>) messages::iterator).spliterator(), false);
            return stream.flatMap(element -> {
                JsonArray message = element.getAsJsonArray();
                int type = message.get(0).getAsNumber().intValue();
                JsonElement data = message.get(1);
                return dispatchMessage(characterId, type, data, send).stream();
            }).collect(Collectors.toList());
        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
            System.out.println(Throwables.getStackTraceAsString(e));
            send.accept(ServerMessage.error("can not parse JSON"));
            return Collections.emptyList();
        }
    }

    private List<GameEvent> dispatchMessage(CharacterId characterId, int type, JsonElement data, Consumer<GameEvent> send) {
        switch (type) {
            case Move:
                return positionCommandHandler.move(characterId, serializer.fromJson(data, Position.class), PositionService.PlayerSpeed);
            case UseSkill:
                SkillUse skillUse = serializer.fromJson(data, SkillUse.class);
                return skillCommandHandler.useSkill(characterId, skillUse.skillId, skillUse.target);
            case Ping:
                return Collections.emptyList();
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
                Player.Id playerId = characterService.getPlayerCharacter(characterId).get().getPlayerId();
                Player.Data playerData = playerService.getPlayer(playerId).getData();
                Player.Data updatedPlayerData = playerData.toBuilder().lastDifficultyLevel(difficultyLevel).build();
                playerService.updatePlayerData(playerId, updatedPlayerData);
                String instanceKey = map + new Random().nextInt();
                arbiter.startInstance(instanceKey, map, address -> send.accept(new JoinToInstance(address.toString())), difficultyLevel);
                return Collections.emptyList();
            case Backdoor:
                return backdoorCommandResolver.resolveCommand(characterId, data, send);
            case GoToHome:
                send.accept(new JoinToInstance(arbiter.getHomeInstnceAddress().toString()));
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    @Value
    private static class SkillUse {
        int skillId;
        CharacterId target;
    }

    @Value
    private static class JoinToInstance implements GameEvent {
        String address;

        @Override
        public int getId() {
            return InstanceCreated;
        }
    }
}