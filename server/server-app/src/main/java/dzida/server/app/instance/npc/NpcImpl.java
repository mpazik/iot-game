package dzida.server.app.instance.npc;

import dzida.server.app.instance.NpcCharacter;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.event.GameEvent;

import java.util.List;

class NpcImpl implements Npc {

    private final NpcCharacter character;
    private final NpcBehaviour npcBehaviour;

    NpcImpl(NpcCharacter character, NpcBehaviour npcBehaviour) {
        this.character = character;
        this.npcBehaviour = npcBehaviour;
    }

    @Override
    public List<InstanceCommand> processTick() {
        return npcBehaviour.processTick(this);
    }

    @Override
    public List<InstanceCommand> processGameEvent(GameEvent gameEvent) {
        return npcBehaviour.processGameEvent(this, gameEvent);
    }

    public NpcCharacter getCharacter() {
        return character;
    }
}


