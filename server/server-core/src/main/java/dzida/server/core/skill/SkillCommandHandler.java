package dzida.server.core.skill;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.CharacterHealed;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectKind;
import dzida.server.core.world.object.WorldObjectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkillCommandHandler {

    private final TimeService timeService;
    private final PositionService positionService;
    private final CharacterService characterService;
    private final SkillService skillService;
    private final WorldObjectService worldObjectService;

    public SkillCommandHandler(
            TimeService timeService,
            PositionService positionService,
            CharacterService characterService,
            SkillService skillService,
            WorldObjectService worldObjectService) {
        this.timeService = timeService;
        this.positionService = positionService;
        this.characterService = characterService;
        this.skillService = skillService;
        this.worldObjectService = worldObjectService;
    }

    public Outcome<List<GameEvent>> useSkillOnCharacter(Id<Character> casterId, Id<Skill> skillId, Id<Character> targetId) {
        if (!characterService.isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }
        if (!characterService.isCharacterLive(targetId)) {
            return Outcome.error("Skill can not be used on a character that is not alive.");
        }

        Skill skill = skillService.getSkill(skillId);
        if (!isReadyForAbility(casterId)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        if (skill.getType() != Skills.Types.ATTACK) {
            return Outcome.error("Server can not understand received message");
        }
        if (!positionService.areCharactersInDistance(casterId, targetId, skill.getRange(), timeService.getCurrentMillis())) {
            return Outcome.error("You are out of range");
        }
        if (!canTargetBeTargeted(skill, casterId, targetId)) {
            return Outcome.error("You can not use ability on that target");
        }
        return Outcome.ok(handleAttackOnCreature(casterId, skill, targetId));
    }

    public Outcome<List<GameEvent>> buildObject(Id<Character> casterId, Id<WorldObjectKind> objectKindId, double x, double y) {
        if (!characterService.isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }

        if (!isReadyForAbility(casterId)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        Optional<GeneralEntity<WorldObject>> worldObject = worldObjectService.createWorldObject(objectKindId, (int) x, (int) y);
        if (!worldObject.isPresent()) {
            return Outcome.error("You can not build object on that position");
        }
        return Outcome.ok(ImmutableList.of(
                new WorldObjectCreated(worldObject.get())
        ));
    }

    public Outcome<List<GameEvent>> useSkillOnWorldObject(Id<Character> casterId, Id<Skill> skillId, Id<WorldObject> targetId) {
        if (!characterService.isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }

        Skill skill = skillService.getSkill(skillId);
        if (!isReadyForAbility(casterId)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        if (skill.getType() != Skills.Types.GATHER) {
            return Outcome.error("Server can not understand received message");
        }
        return Outcome.ok(ImmutableList.of(
                new SkillUsedOnWorldObject(casterId, skill.getId(), targetId),
                new WorldObjectRemoved(worldObjectService.getObject(targetId))
        ));
    }

    private List<GameEvent> handleAttackOnCreature(Id<Character> casterId, Skill skill, Id<Character> targetId) {
        double damage = skill.getDamage();
        List<GameEvent> events = new ArrayList<>();
        events.add(new CharacterGotDamage(targetId, damage));
        events.add(new SkillUsedOnCharacter(casterId, skill.getId(), targetId));
        if (skillService.getHealth(targetId) <= damage) {
            // the got damage event must be before the died event
            events.add(new CharacterDied(targetId));
        }
        return events;
    }

    private boolean canTargetBeTargeted(Skill skill, Id<Character> casterId, Id<Character> targetId) {
        switch (skill.getTarget()) {
            case Skills.Target.ENEMIES:
                return characterService.isCharacterEnemyFor(casterId, targetId);
            default:
                return false;
        }
    }

    private boolean isReadyForAbility(Id<Character> casterId) {
        return !skillService.isOnCooldown(casterId, timeService.getCurrentMillis());
    }

    public Outcome<List<GameEvent>> eatApple(Id<Character> casterId) {
        if (!characterService.isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }

        int missingHealth = skillService.getMaxHealth(casterId) - skillService.getHealth(casterId);
        int appleHealingValue = 50;
        int toHeal = Math.min(missingHealth, appleHealingValue);
        return Outcome.ok(ImmutableList.of(new CharacterHealed(casterId, toHeal)));
    }

    public Outcome<List<GameEvent>> eatRottenApple(Id<Character> casterId) {
        if (!characterService.isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }

        int damage = 10;
        List<GameEvent> events = new ArrayList<>();
        events.add(new CharacterGotDamage(casterId, damage));
        events.add(new ServerMessage("You ate rotten apple."));
        if (skillService.getHealth(casterId) <= damage) {
            // the got damage event must be before the died event
            events.add(new CharacterDied(casterId));
        }

        return Outcome.ok(events);
    }
}
