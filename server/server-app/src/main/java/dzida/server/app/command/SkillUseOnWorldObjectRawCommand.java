package dzida.server.app.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.skill.Skill;
import dzida.server.core.world.object.WorldObject;

public class SkillUseOnWorldObjectRawCommand implements CharacterCommand {
    public final Id<Skill> skillId;
    public final Id<WorldObject> target;

    SkillUseOnWorldObjectRawCommand(Id<Skill> skillId, Id<WorldObject> target) {
        this.skillId = skillId;
        this.target = target;
    }

    @Override
    public InstanceCommand getInstanceCommand(Id<Character> characterId) {
        return new SkillUseOnWorldObjectCommand(characterId, skillId, target);
    }
}
