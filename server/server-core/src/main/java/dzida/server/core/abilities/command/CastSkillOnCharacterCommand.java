package dzida.server.core.abilities.command;

import dzida.server.core.entity.EntityId;
import dzida.server.core.entity.Command;
import dzida.server.core.abilities.Abilities;

public class CastSkillOnCharacterCommand implements Command {
    public final EntityId<Abilities> targetId;

    public CastSkillOnCharacterCommand(EntityId<Abilities> targetId) {
        this.targetId = targetId;
    }
}
