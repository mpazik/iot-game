package dzida.server.core.character.event;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.CharacterEvent;
import dzida.server.core.skill.SkillService.SkillData;
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
