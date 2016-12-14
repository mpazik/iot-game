package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import org.jetbrains.annotations.NotNull;

public class SkillUsedOnWorldMap implements CharacterEvent {
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

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return casterId;
    }
}
