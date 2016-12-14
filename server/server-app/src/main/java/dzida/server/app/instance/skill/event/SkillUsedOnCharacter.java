package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import org.jetbrains.annotations.NotNull;

public class SkillUsedOnCharacter implements CharacterEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Id<Character> targetId;

    public SkillUsedOnCharacter(Id<Character> casterId, Id<Skill> skillId, Id<Character> targetId) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.targetId = targetId;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return casterId;
    }
}
