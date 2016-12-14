package dzida.server.app.instance.skill.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.world.object.WorldObject;
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
