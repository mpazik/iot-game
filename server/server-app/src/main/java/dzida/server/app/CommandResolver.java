package dzida.server.app;

import dzida.server.app.command.InstanceCommand;
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

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class CommandResolver {
    private final PositionCommandHandler positionCommandHandler;
    private final SkillCommandHandler skillCommandHandler;
    private final CharacterCommandHandler characterCommandHandler;
    private final BackdoorCommandResolver backdoorCommandResolver;
    private final ChatService chatService;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler,
            ChatService chatService) {
        this.positionCommandHandler = positionCommandHandler;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;
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

    public List<GameEvent> handleCommand(InstanceCommand commandToProcess, Id<Player> playerId, Id<Character> characterId) {
        return whenTypeOf(commandToProcess)

                .is(MoveCommand.class)
                .thenReturn(command -> positionCommandHandler.move(characterId, command.asPoint(), PositionService.PlayerSpeed))

                .is(SkillUseOnCharacterCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnCharacter(characterId, command.skillId, command.target))

                .is(SkillUseOnWorldMapCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldMap(characterId, command.skillId, command.x, command.y))

                .is(SkillUseOnWorldObjectCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldObject(characterId, command.skillId, command.target))

                .is(BackdoorCommandResolver.BackdoorCommand.class)
                .thenReturn(command -> backdoorCommandResolver.resolveCommand(characterId, command))

                .is(SendMessageCommand.class)
                .thenReturn(command -> chatService.handleMessage(playerId, command.message))

                .get();
    }
}