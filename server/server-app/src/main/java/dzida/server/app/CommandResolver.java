package dzida.server.app;

import dzida.server.app.command.Command;
import dzida.server.app.command.JoinBattleCommand;
import dzida.server.app.command.MoveCommand;
import dzida.server.app.command.SendMessageCommand;
import dzida.server.app.command.SkillUseOnCharacterCommand;
import dzida.server.app.command.SkillUseOnWorldMapCommand;
import dzida.server.app.command.SkillUseOnWorldObjectCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.model.Character;
import dzida.server.core.chat.ChatService;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.SkillCommandHandler;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class CommandResolver {
    private final PositionCommandHandler positionCommandHandler;
    private final TimeSynchroniser timeSynchroniser;
    private final SkillCommandHandler skillCommandHandler;
    private final CharacterCommandHandler characterCommandHandler;
    private final Container container;
    private final BackdoorCommandResolver backdoorCommandResolver;
    private final PlayerService playerService;
    private final ChatService chatService;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler,
            TimeSynchroniser timeSynchroniser,
            Container container, PlayerService playerService, ChatService chatService) {
        this.positionCommandHandler = positionCommandHandler;
        this.timeSynchroniser = timeSynchroniser;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;
        this.container = container;
        this.playerService = playerService;
        this.chatService = chatService;

        if (Configuration.isDevMode()) {
            backdoorCommandResolver = new BackdoorCommandResolver();
        } else {
            backdoorCommandResolver = BackdoorCommandResolver.NoOpResolver;
        }
    }

    public List<GameEvent> createCharacter(Character character) {
        return characterCommandHandler.spawnCharacter(character);
    }

    public List<GameEvent> removeCharacter(Id<Character> characterId) {
        return characterCommandHandler.killCharacter(characterId);
    }

    public List<GameEvent> handleCommand(Command commandToProcess, Id<Player> playerId, Id<Character> characterId, Consumer<GameEvent> send) {
        return whenTypeOf(commandToProcess)

                .is(MoveCommand.class)
                .thenReturn(command -> positionCommandHandler.move(characterId, command.asPoint(), PositionService.PlayerSpeed))

                .is(SkillUseOnCharacterCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnCharacter(characterId, command.skillId, command.target))

                .is(SkillUseOnWorldMapCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldMap(characterId, command.skillId, command.x, command.y))

                .is(SkillUseOnWorldObjectCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldObject(characterId, command.skillId, command.target))

                .is(TimeSynchroniser.TimeSyncRequest.class)
                .thenReturn(command -> {
                    TimeSynchroniser.TimeSyncResponse timeSyncResponse = timeSynchroniser.timeSync(command);
                    send.accept(timeSyncResponse);
                    return Collections.emptyList();
                })

                .is(JoinBattleCommand.class)
                .thenReturn(command -> {
                    Player.Data playerData = playerService.getPlayer(playerId).getData();
                    Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), command.difficultyLevel);
                    playerService.updatePlayerData(playerId, updatedPlayerData);
                    Key<Instance> instanceKey = container.startInstance(command.map, command.difficultyLevel);
                    return Collections.singletonList(new JoinToInstance(instanceKey));
                })

                .is(BackdoorCommandResolver.BackdoorCommand.class)
                .thenReturn(command -> backdoorCommandResolver.resolveCommand(characterId, command, send))

                .is(SendMessageCommand.class)
                .thenReturn(command -> chatService.handleMessage(playerId, command.message))

                .get();
    }

    private static class JoinToInstance implements GameEvent {
        final Key<Instance> instanceKey;

        JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }

        @Override
        public int getId() {
            return InstanceCreated;
        }
    }
}