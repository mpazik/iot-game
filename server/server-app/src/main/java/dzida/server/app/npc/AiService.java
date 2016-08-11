package dzida.server.app.npc;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.event.GameEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AiService {
    private final NpcBehaviour npcBehaviour;
    private final Map<Id<Character>, Npc> npcs = new HashMap<>();

    public AiService(NpcBehaviour npcBehaviour) {
        this.npcBehaviour = npcBehaviour;
    }

    public void createNpc(int npcType, NpcCharacter character) {
        // in future there will be different behaviour here.
        if (npcType == Npc.Fighter) {
            npcs.put(character.getId(), new NpcImpl(character, npcBehaviour));
            return;
        }
        if (npcType == Npc.Archer) {
            npcs.put(character.getId(), new NpcImpl(character, npcBehaviour));
            return;
        }

        throw new IllegalArgumentException();
    }

    public void removeNpc(Id<Character> characterId) {
        npcs.remove(characterId);
    }

    public List<GameEvent> processTick() {
        return npcs.values().stream().flatMap(npc -> npc.processTick().stream()).collect(Collectors.toList());
    }

    public List<GameEvent> processPacket(Id<Character> characterId, GameEvent event) {
        return npcs.get(characterId).processGameEvent(event);
    }
}
