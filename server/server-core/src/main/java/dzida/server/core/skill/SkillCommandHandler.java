package dzida.server.core.skill;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dzida.server.core.event.ServerMessage.error;
import static dzida.server.core.event.ServerMessage.info;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

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

    public List<GameEvent> useSkillOnCharacter(CharacterId casterId, Id<Skill> skillId, CharacterId targetId) {
        if (!characterService.isCharacterLive(casterId) || !characterService.isCharacterLive(targetId)) {
            return emptyList();
        }

        Skill skill = Skills.get(skillId);
        if (!isReadyForAbility(casterId))
            return singletonList(info("You are not ready yet to use ability"));
        if (skill.getType() == Skills.Types.ATTACK) {
            if (!positionService.areCharactersInDistance(casterId, targetId, skill.getRange(), timeService.getCurrentMillis())) {
                return singletonList(info("You are out of range"));
            }
            if (!canTargetBeTargeted(skill, casterId, targetId)) {
                return singletonList(info("You can not use ability on that target"));
            }
            return handleAttackOnCreature(casterId, skill, targetId);
        }
        return singletonList(error("Server can not understand received message"));
    }

    public List<GameEvent> useSkillOnWorldMap(CharacterId casterId, Id<Skill> skillId, double x, double y) {
        if (!characterService.isCharacterLive(casterId)) {
            return emptyList();
        }

        Skill skill = Skills.get(skillId);
        if (!isReadyForAbility(casterId))
            return singletonList(info("You are not ready yet to use ability"));
        if (skill.getType() == Skills.Types.BUILDING) {
            Optional<GeneralEntity<WorldObject>> worldObject = worldObjectService.createWorldObject(skill.getWorldObject(), (int) x, (int) y);
            if (!worldObject.isPresent()) {
                return singletonList(info("You can not build object on that position"));
            }
            return ImmutableList.of(new SkillUsedOnWorldMap(casterId, skill.getId(), x, y), new WorldObjectCreated(worldObject.get()));
        }
        return singletonList(error("Server can not understand received message"));
    }

    public List<GameEvent> useSkillOnWorldObject(CharacterId casterId, Id<Skill> skillId, Id<WorldObject> targetId) {
        if (!characterService.isCharacterLive(casterId)) {
            return emptyList();
        }

        Skill skill = Skills.get(skillId);
        if (!isReadyForAbility(casterId))
            return singletonList(info("You are not ready yet to use ability"));
        if (skill.getType() == Skills.Types.GATHER) {
            return ImmutableList.of(new SkillUsedOnWorldObject(casterId, skill.getId(), targetId), new WorldObjectRemoved(targetId));
        }
        return singletonList(error("Server can not understand received message"));
    }

    private List<GameEvent> handleAttackOnCreature(CharacterId casterId, Skill skill, CharacterId targetId) {
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

    private boolean canTargetBeTargeted(Skill skill, CharacterId casterId, CharacterId targetId) {
        switch (skill.getTarget()) {
            case Skills.Target.ENEMIES:
                return characterService.isCharacterEnemyFor(casterId, targetId);
            default:
                return false;
        }
    }

    private boolean isReadyForAbility(CharacterId casterId) {
        return !skillService.isOnCooldown(casterId, timeService.getCurrentMillis());
    }
}
