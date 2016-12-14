package dzida.server.app.instance.character.event;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.unit.Move;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.skill.SkillService.SkillData;
import org.jetbrains.annotations.NotNull;

public class CharacterSpawned implements CharacterEvent {
    public final Character character;
    public final Move move;
    public final SkillData skillData;

    public CharacterSpawned(Character character, Move move, SkillData skillData) {
        this.character = character;
        this.move = move;
        this.skillData = skillData;
    }

    @NotNull
    @Override
    public Id<Character> getCharacterId() {
        return character.getId();
    }
}
