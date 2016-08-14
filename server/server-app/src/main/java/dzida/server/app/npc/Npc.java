package dzida.server.app.npc;

import dzida.server.app.command.InstanceCommand;
import dzida.server.core.event.GameEvent;

import java.util.Collections;
import java.util.List;

public interface Npc {
    int Fighter = 0;
    int Archer = 1;

    default List<InstanceCommand> processTick() {
        return Collections.emptyList();
    }

    default List<InstanceCommand> processGameEvent(GameEvent gameEvent) {
        return Collections.emptyList();
    }
}
