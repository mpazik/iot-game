package dzida.server.app.instance.command;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.skill.Skills;
import dzida.server.app.instance.skill.event.SkillUsedOnWorldObject;
import dzida.server.app.instance.world.WorldObjectRemoved;
import dzida.server.app.instance.world.object.WorldObject;

import java.util.List;

public class SkillUseOnWorldObjectCommand implements InstanceCommand {
    public final Id<Skill> skillId;
    public final Id<WorldObject> target;
    public final Id<Character> characterId;

    public SkillUseOnWorldObjectCommand(Id<Character> characterId, Id<Skill> skillId, Id<WorldObject> target) {
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

        Skill skill = definitions.getSkill(skillId);
        if (state.getSkill().isOnCooldown(casterId, currentTime)) {
            return Outcome.error("You are not ready yet to use ability");
        }
        if (skill.getType() != Skills.Types.GATHER) {
            return Outcome.error("Server can not understand received message");
        }
        return Outcome.ok(ImmutableList.of(
                new SkillUsedOnWorldObject(casterId, skill.getId(), target, currentTime),
                new WorldObjectRemoved(state.getWorld().getObject(target))
        ));
    }
}
