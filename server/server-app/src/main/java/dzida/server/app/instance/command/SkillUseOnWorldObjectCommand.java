package dzida.server.app.instance.command;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.world.object.WorldObject;

public class SkillUseOnWorldObjectCommand implements InstanceCommand {
    public final Id<Skill> skillId;
    public final Id<WorldObject> target;
    public final Id<Character> characterId;

    public SkillUseOnWorldObjectCommand(Id<Character> characterId, Id<Skill> skillId, Id<WorldObject> target) {
        this.skillId = skillId;
        this.target = target;
        this.characterId = characterId;
    }
}
