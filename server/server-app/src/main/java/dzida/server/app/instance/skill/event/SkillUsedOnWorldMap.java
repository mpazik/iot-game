package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SkillUsedOnWorldMap implements CharacterEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Long timestamp;
    double x;
    double y;

    public SkillUsedOnWorldMap(Id<Character> casterId, Id<Skill> skillId, double x, double y, Long timestamp) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return casterId;
    }

    @Nonnull
    @Override
    public GameState updateState(@Nonnull GameState state, GameDefinitions definitions) {
        return state.updateSkill(skillSate -> {
            Skill skill = definitions.getSkill(skillId);
            return skillSate.setCharacterCooldown(casterId, skill, timestamp);
        });
    }
}
