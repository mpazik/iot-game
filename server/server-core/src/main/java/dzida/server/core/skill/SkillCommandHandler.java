package dzida.server.core.skill;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsed;
import dzida.server.core.time.TimeService;

import java.util.ArrayList;
import java.util.List;

import static dzida.server.core.event.ServerMessage.error;
import static dzida.server.core.event.ServerMessage.info;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SkillCommandHandler {

    private final TimeService timeService;
    private final PositionService positionService;
    private final CharacterService characterService;
    private final SkillService skillService;

    public SkillCommandHandler(TimeService timeService, PositionService positionService, CharacterService characterService, SkillService skillService) {
        this.timeService = timeService;
        this.positionService = positionService;
        this.characterService = characterService;
        this.skillService = skillService;
    }

    public List<GameEvent> useSkillOnCharacter(CharacterId casterId, Id<Skill> skillId, CharacterId targetId) {
        if (!characterService.isCharacterLive(casterId) || !characterService.isCharacterLive(targetId)) {
            return emptyList();
        }

        Skill skill = skillService.getSkill(skillId);
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

    private List<GameEvent> handleAttackOnCreature(CharacterId casterId, Skill skill, CharacterId targetId) {
        double damage = skill.getDamage();
        List<GameEvent> events = new ArrayList<>();
        events.add(new CharacterGotDamage(targetId, damage));
        events.add(new SkillUsed(casterId, skill.getId(), targetId));
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
