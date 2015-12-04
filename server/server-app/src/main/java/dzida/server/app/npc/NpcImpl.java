package dzida.server.app.npc;

import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.event.GameEvent;

import java.util.List;

class NpcImpl implements Npc {

    private final NpcCharacter character;
    private final NpcBehaviour npcBehaviour;

    NpcImpl(NpcCharacter character, NpcBehaviour npcBehaviour) {
        this.character = character;
        this.npcBehaviour = npcBehaviour;
    }

    @Override
    public List<GameEvent> processTick() {
        return npcBehaviour.processTick(this);
    }

    @Override
    public List<GameEvent> processGameEvent(GameEvent gameEvent) {
        return npcBehaviour.processGameEvent(this, gameEvent);
    }

    public NpcCharacter getCharacter() {
        return character;
    }
}


