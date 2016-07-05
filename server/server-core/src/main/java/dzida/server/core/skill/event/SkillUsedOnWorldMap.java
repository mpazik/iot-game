package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;

public class SkillUsedOnWorldMap implements GameEvent {
    public final CharacterId casterId;
    public final Id<Skill> skillId;
    double x;
    double y;

    public SkillUsedOnWorldMap(CharacterId casterId, Id<Skill> skillId, double x, double y) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.x = x;
        this.y = y;
    }

    @Override
    public int getId() {
        return GameEvent.SkillUsedOnWorldMap;
    }
}
