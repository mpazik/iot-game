package dzida.server.app.instance.command;

import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.CharacterDied;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.skill.Skills;
import dzida.server.app.instance.skill.event.CharacterGotDamage;
import dzida.server.app.instance.skill.event.SkillUsedOnCharacter;

import java.util.ArrayList;
import java.util.List;

public class SkillUseOnCharacterCommand implements InstanceCommand {
    public final Id<Skill> skillId;
    public final Id<Character> target;
    public final Id<Character> characterId;

    public SkillUseOnCharacterCommand(Id<Character> characterId, Id<Skill> skillId, Id<Character> target) {
        this.skillId = skillId;
        this.target = target;
        this.characterId = characterId;
    }

    @Override
    public Outcome<List<GameEvent>> process(GameState state, GameDefinitions definitions, Long currentTime) {
        Id<Character> casterId = characterId;
        if (!state.getCharacter().isCharacterLive(casterId)) {
            return Outcome.error("Skill can not be used by a not living character.");
        }
        if (!state.getCharacter().isCharacterLive(target)) {
            return Outcome.error("Skill can not be used on a character that is not alive.");
        }

        Skill skill = definitions.getSkill(skillId);
        if (state.getSkill().isOnCooldown(casterId, currentTime)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        if (skill.getType() != Skills.Types.ATTACK) {
            return Outcome.error("Server can not understand received message");
        }
        if (!state.getPosition().areCharactersInDistance(casterId, target, skill.getRange(), currentTime)) {
            return Outcome.error("You are out of range");
        }
        if (!canTargetBeTargeted(state, skill, casterId, target)) {
            return Outcome.error("You can not use ability on that target");
        }
        return Outcome.ok(handleAttackOnCreature(state, casterId, skill, target, currentTime));
    }

    private List<GameEvent> handleAttackOnCreature(GameState state, Id<Character> casterId, Skill skill, Id<Character> targetId, Long currentTime) {
        double damage = skill.getDamage();
        List<GameEvent> events = new ArrayList<>();
        events.add(new CharacterGotDamage(targetId, damage));
        events.add(new SkillUsedOnCharacter(casterId, skill.getId(), targetId, currentTime));
        if (state.getSkill().getHealth(targetId) <= damage) {
            // the got damage event must be before the died event
            events.add(new CharacterDied(targetId));
        }
        return events;
    }

    private boolean canTargetBeTargeted(GameState state, Skill skill, Id<Character> casterId, Id<Character> targetId) {
        switch (skill.getTarget()) {
            case Skills.Target.ENEMIES:
                return state.getCharacter().isCharacterEnemyFor(casterId, targetId);
            default:
                return false;
        }
    }
}
