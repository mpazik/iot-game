package dzida.server.app.instance.npc;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.NpcCharacter;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.event.GameEvent;

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

    public List<InstanceCommand> processTick() {
        return npcs.values().stream().flatMap(npc -> npc.processTick().stream()).collect(Collectors.toList());
    }

    public List<InstanceCommand> processChange(GameEvent event) {
        return npcs.values()
                .stream()
                .flatMap(npc -> npc.processGameEvent(event).stream())
                .collect(Collectors.toList());
    }
}
