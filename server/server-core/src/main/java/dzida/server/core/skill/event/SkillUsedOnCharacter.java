package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;

public class SkillUsedOnCharacter implements GameEvent {
    public final CharacterId casterId;
    public final Id<Skill> skillId;
    public final CharacterId targetId;

    public SkillUsedOnCharacter(CharacterId casterId, Id<Skill> skillId, CharacterId targetId) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.targetId = targetId;
    }

    @Override
    public int getId() {
        return GameEvent.SkillUsedOnCharacter;
    }
}
