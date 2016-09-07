package dzida.server.core.skill.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.CharacterEvent;
import dzida.server.core.skill.Skill;
import dzida.server.core.world.object.WorldObject;
import org.jetbrains.annotations.NotNull;

public class SkillUsedOnWorldObject implements CharacterEvent {
    public final Id<Character> casterId;
    public final Id<Skill> skillId;
    public final Id<WorldObject> worldObjectId;

    public SkillUsedOnWorldObject(Id<Character> casterId, Id<Skill> skillId, Id<WorldObject> worldObjectId) {
        this.casterId = casterId;
        this.skillId = skillId;
        this.worldObjectId = worldObjectId;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return casterId;
    }
}
