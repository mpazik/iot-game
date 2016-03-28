package dzida.server.core.abilities;

import dzida.server.core.abilities.change.DiedChange;
import dzida.server.core.abilities.change.GotDamageChange;
import dzida.server.core.abilities.change.SkillUsedChange;
import dzida.server.core.abilities.change.SpawnedChange;
import dzida.server.core.abilities.command.AttackCharacterCommand;
import dzida.server.core.abilities.command.CastSkillCommand;
import dzida.server.core.abilities.command.CastSkillOnCharacterCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.command.KillCharacterCommand;
import dzida.server.core.character.command.SpawnCharacterCommand;
import dzida.server.core.character.model.Character;
import dzida.server.core.entity.Command;
import dzida.server.core.entity.CommandProcessor;
import dzida.server.core.entity.EntityId;
import dzida.server.core.entity.StateRepository;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillService;
import dzida.server.core.skill.Skills;
import dzida.server.core.time.TimeService;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class AbilitiesCommandProcessor implements CommandProcessor<Abilities> {
    private final CharacterService characterService;
    private final SkillService skillService;
    private final StateRepository<Abilities> characterSkills;
    private final TimeService timeService;

    public AbilitiesCommandProcessor(
            CharacterService characterService,
            SkillService skillService,
            StateRepository<Abilities> characterSkills,
            TimeService timeService) {
        this.characterService = characterService;
        this.skillService = skillService;
        this.characterSkills = characterSkills;
        this.timeService = timeService;
    }

    @Override
    public Result<Abilities> process(Command commandToHandle) {
        return whenTypeOf(commandToHandle)
                .is(SpawnCharacterCommand.class).thenReturn(this::handleSpawnCharacter)
                .is(KillCharacterCommand.class).thenReturn(this::handleKillCharacter)
                .is(CastSkillCommand.class).thenReturn(this::handleUseSkill)
                .is(AttackCharacterCommand.class).thenReturn(this::handleAttackOnCharacter)
                .get();
    }

    private Result<Abilities> handleSpawnCharacter(SpawnCharacterCommand command) {
        int characterType = characterService.getCharacterType(new CharacterId((int) command.characterId.getValue()));
        SkillService.SkillData data = skillService.getInitialSkillData(characterType);

        // todo it probably should be generated and somehow related to the characterId
        EntityId<Abilities> characterSkillsId = idForCharacter(command.characterId);
        SpawnedChange spawnedEvent = new SpawnedChange(data.getMaxHealth());
        return Result.change(characterSkillsId, spawnedEvent);
    }

    private Result<Abilities> handleKillCharacter(KillCharacterCommand command) {
        EntityId<Abilities> characterSkillsId = idForCharacter(command.characterId);
        return Result.change(characterSkillsId, new DiedChange());
    }

    private Result<Abilities> handleUseSkill(CastSkillCommand command) {
        Abilities state = characterSkills.getState(command.casterId);
        Id<Skill> skillId = command.skillId;
        Skill skill = Skills.get(skillId);

        if (!state.canUseSkill(skill, timeService.now())) {
            return Result.empty();
        }
        Result.Builder<Abilities> builder = Result.<Abilities>builder();
        builder.addChange(command.casterId, new SkillUsedChange(skillId, timeService.now()));

        whenTypeOf(command).is(CastSkillOnCharacterCommand.class).then(cast -> {
            int damage = skill.getDamage();
            builder.addCommand(new AttackCharacterCommand(cast.targetId, damage));
        });

        return builder.build();
    }

    private Result<Abilities> handleAttackOnCharacter(AttackCharacterCommand command) {
        Abilities state = characterSkills.getState(command.targetId);
        if (state.getHealth() <= command.damage) {
            return Result.command(new KillCharacterCommand(characterIdForId(command.targetId)));
        }
        return Result.change(command.targetId, new GotDamageChange(command.damage));
    }

    private EntityId<Abilities> idForCharacter(Id<Character> id) {
        return new EntityId<>(id.getValue());
    }

    private Id<Character> characterIdForId(EntityId<Abilities> id) {
        return new Id<>(id.getValue());
    }
}
