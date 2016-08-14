package dzida.server.app.command;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.skill.Skill;

public class SkillUseOnWorldMapRawCommand implements CharacterCommand {
    public final Id<Skill> skillId;
    public final double x;
    public final double y;

    private SkillUseOnWorldMapRawCommand(Id<Skill> skillId, double x, double y) {
        this.skillId = skillId;
        this.x = x;
        this.y = y;
    }

    @Override
    public InstanceCommand getInstanceCommand(Id<Character> characterId) {
        return new SkillUseOnWorldMapCommand(characterId, skillId, x, y);
    }
}
