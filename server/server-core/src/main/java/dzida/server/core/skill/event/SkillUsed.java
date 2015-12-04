package dzida.server.core.skill.event;

import dzida.server.core.CharacterId;
import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class SkillUsed implements GameEvent {
    CharacterId casterId;
    int skillId;
    CharacterId targetId;

    @Override
    public int getId() {
        return GameEvent.SkillUsed;
    }
}
