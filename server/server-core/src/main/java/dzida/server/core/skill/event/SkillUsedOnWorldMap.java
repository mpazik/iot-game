package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;

public class SkillUsedOnWorldMap implements GameEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    double x;
    double y;

    public SkillUsedOnWorldMap(Id<Character> casterId, Id<Skill> skillId, double x, double y) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.x = x;
        this.y = y;
    }
}
