package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.Skill;

public class SkillUsedOnCharacter implements GameEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Id<Character> targetId;

    public SkillUsedOnCharacter(Id<Character> casterId, Id<Skill> skillId, Id<Character> targetId) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.targetId = targetId;
    }

    @Override
    public int getId() {
        return GameEvent.SkillUsedOnCharacter;
    }
}
