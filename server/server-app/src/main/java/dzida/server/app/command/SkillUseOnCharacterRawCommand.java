package dzida.server.app.command;

import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.SkillUseOnCharacterCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.skill.Skill;

public class SkillUseOnCharacterRawCommand implements CharacterCommand {
    public final Id<Skill> skillId;
    public final Id<Character> target;

    public SkillUseOnCharacterRawCommand(Id<Skill> skillId, Id<Character> target) {
        this.skillId = skillId;
        this.target = target;
    }

    @Override
    public InstanceCommand getInstanceCommand(Id<Character> characterId) {
        return new SkillUseOnCharacterCommand(characterId, skillId, target);
    }
}
