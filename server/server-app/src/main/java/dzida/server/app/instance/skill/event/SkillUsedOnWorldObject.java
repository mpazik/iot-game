package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.GameDefinitions;
import dzida.server.app.instance.GameState;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.world.object.WorldObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SkillUsedOnWorldObject implements CharacterEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Id<WorldObject> worldObjectId;
    public final Long currentTime;

    public SkillUsedOnWorldObject(Id<Character> casterId, Id<Skill> skillId, Id<WorldObject> worldObjectId, Long currentTime) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.worldObjectId = worldObjectId;
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
