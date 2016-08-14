package dzida.server.app;

import dzida.server.app.command.SpawnCharacterCommand;
import dzida.server.app.command.InstanceCommand;
import dzida.server.app.command.MoveCommand;
import dzida.server.app.command.KillCharacterCommand;
import dzida.server.app.command.SkillUseOnCharacterCommand;
import dzida.server.app.command.SkillUseOnWorldMapCommand;
import dzida.server.app.command.SkillUseOnWorldObjectCommand;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.SkillCommandHandler;

import java.util.List;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class CommandResolver {
    private final PositionCommandHandler positionCommandHandler;
    private final SkillCommandHandler skillCommandHandler;
    private final CharacterCommandHandler characterCommandHandler;
    private final BackdoorCommandResolver backdoorCommandResolver;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler) {
        this.positionCommandHandler = positionCommandHandler;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;

        if (Configuration.isDevMode()) {
            backdoorCommandResolver = new BackdoorCommandResolver();
        } else {
            backdoorCommandResolver = BackdoorCommandResolver.NoOpResolver;
        }
    }

    public List<GameEvent> handleCommand(InstanceCommand commandToProcess) {
        return whenTypeOf(commandToProcess)

                .is(MoveCommand.class)
                .thenReturn(command -> {
                    double speed = command.speed == 0 ? PositionService.PlayerSpeed: command.speed;
                    return positionCommandHandler.move(command.characterId, command.asPoint(), speed);
                })

                .is(SkillUseOnCharacterCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnCharacter(command.characterId, command.skillId, command.target))

                .is(SkillUseOnWorldMapCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldMap(command.characterId, command.skillId, command.x, command.y))

                .is(SkillUseOnWorldObjectCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldObject(command.characterId, command.skillId, command.target))

                .is(BackdoorCommandResolver.BackdoorCommand.class)
                .thenReturn(command -> backdoorCommandResolver.resolveCommand(command.characterId, command))

                .is(SpawnCharacterCommand.class)
                .thenReturn(command -> characterCommandHandler.spawnCharacter(command.character, command.spawnPoint))

                .is(KillCharacterCommand.class)
                .thenReturn(command -> characterCommandHandler.killCharacter(command.characterId))

                .get();
    }
}