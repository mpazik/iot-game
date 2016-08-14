package dzida.server.app.instance.scenario;

import dzida.server.app.instance.InstanceStateManager;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.npc.AiService;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.NpcCharacter;

import java.util.List;
import java.util.function.Consumer;

public class NpcScenarioLogic {
    private final AiService aiService;

    public NpcScenarioLogic(AiService aiService, InstanceStateManager instanceStateManager, Consumer<InstanceCommand> instanceCommandConsumer) {
        this.aiService = aiService;
        instanceStateManager.getEventPublisher().subscribe(event -> {
            List<InstanceCommand> instanceCommands = aiService.processChange(event);
            instanceCommands.forEach(instanceCommandConsumer);
        });
    }

    public Character addNpc(int npcType) {
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));
        NpcCharacter character = new NpcCharacter(characterId, npcType);
        aiService.createNpc(npcType, character);
        return character;
    }

    public void removeNpc(Id<Character> characterId) {
        aiService.removeNpc(characterId);
    }
}
