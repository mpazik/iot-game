package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SkillUsedOnCharacter implements CharacterEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Id<Character> targetId;
    public final Long currentTime;

    public SkillUsedOnCharacter(Id<Character> casterId, Id<Skill> skillId, Id<Character> targetId, Long currentTime) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.targetId = targetId;
        this.currentTime = currentTime;
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
            return skillSate.setCharacterCooldown(casterId, skill, currentTime);
        });
    }
}
