package dzida.server.app.instance;

import dzida.server.app.Configuration;
import dzida.server.app.basic.Outcome;
import dzida.server.app.instance.character.CharacterCommandHandler;
import dzida.server.app.instance.command.BuildObjectCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.KillCharacterCommand;
import dzida.server.app.instance.command.MoveCommand;
import dzida.server.app.instance.command.SkillUseOnCharacterCommand;
import dzida.server.app.instance.command.SkillUseOnWorldObjectCommand;
import dzida.server.app.instance.command.SpawnCharacterCommand;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.PositionCommandHandler;
import dzida.server.app.instance.position.PositionService;
import dzida.server.app.instance.skill.SkillCommandHandler;
import dzida.server.app.parcel.ParcelCommand;
import dzida.server.app.parcel.ParcelCommandHandler;

import java.util.List;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class CommandResolver {
    private final PositionCommandHandler positionCommandHandler;
    private final SkillCommandHandler skillCommandHandler;
    private final CharacterCommandHandler characterCommandHandler;
    private final ParcelCommandHandler parcelCommandHandler;
    private final BackdoorCommandResolver backdoorCommandResolver;

    public CommandResolver(
            PositionCommandHandler positionCommandHandler,
            SkillCommandHandler skillCommandHandler,
            CharacterCommandHandler characterCommandHandler, ParcelCommandHandler parcelCommandHandler) {
        this.positionCommandHandler = positionCommandHandler;
        this.skillCommandHandler = skillCommandHandler;
        this.characterCommandHandler = characterCommandHandler;
        this.parcelCommandHandler = parcelCommandHandler;

        if (Configuration.isDevMode()) {
            backdoorCommandResolver = new BackdoorCommandResolver();
        } else {
            backdoorCommandResolver = BackdoorCommandResolver.NoOpResolver;
        }
    }

    public Outcome<List<GameEvent>> handleCommand(InstanceCommand commandToProcess) {
        return whenTypeOf(commandToProcess)

                .is(MoveCommand.class)
                .thenReturn(command -> {
                    double speed = command.speed == 0 ? PositionService.PlayerSpeed: command.speed;
                    return positionCommandHandler.move(command.characterId, command.asPoint(), speed);
                })

                .is(SkillUseOnCharacterCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnCharacter(command.characterId, command.skillId, command.target))

                .is(BuildObjectCommand.class)
                .thenReturn(command -> skillCommandHandler.buildObject(command.characterId, command.objectKindId, command.x, command.y))

                .is(SkillUseOnWorldObjectCommand.class)
                .thenReturn(command -> skillCommandHandler.useSkillOnWorldObject(command.characterId, command.skillId, command.target))

                .is(InstanceCommand.EatAppleCommand.class)
                .thenReturn(command -> skillCommandHandler.eatApple(command.characterId))

                .is(InstanceCommand.EatRottenAppleCommand.class)
                .thenReturn(command -> skillCommandHandler.eatRottenApple(command.characterId))

                .is(BackdoorCommandResolver.BackdoorCommand.class)
                .thenReturn(command -> backdoorCommandResolver.resolveCommand(command.characterId, command))

                .is(SpawnCharacterCommand.class)
                .thenReturn(command -> characterCommandHandler.spawnCharacter(command.character, command.spawnPoint))

                .is(KillCharacterCommand.class)
                .thenReturn(command -> characterCommandHandler.killCharacter(command.characterId))

                .is(ParcelCommand.ClaimParcel.class)
                .thenReturn(parcelCommandHandler::claimLand)
                .get();
    }
}