package dzida.server.core.abilities.command;

import dzida.server.core.abilities.Abilities;
import dzida.server.core.entity.Command;
import dzida.server.core.entity.EntityId;

public class AttackCharacterCommand implements Command {
    public final EntityId<Abilities> targetId;
    public final int damage;

    public AttackCharacterCommand(EntityId<Abilities> targetId, int damage) {
        this.targetId = targetId;
        this.damage = damage;
    }
}
