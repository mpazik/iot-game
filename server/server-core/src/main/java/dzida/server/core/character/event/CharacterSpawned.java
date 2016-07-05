package dzida.server.core.character.event;

import dzida.server.core.basic.unit.Move;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.SkillService.SkillData;

public class CharacterSpawned implements GameEvent {
    public final Character character;
    public final Move move;
    public final SkillData skillData;

    public CharacterSpawned(Character character, Move move, SkillData skillData) {
        this.character = character;
        this.move = move;
        this.skillData = skillData;
    }

    @Override
    public int getId() {
        return GameEvent.CharacterSpawned;
    }
}
