package dzida.server.app.scenario;

import dzida.server.app.CommandResolver;
import dzida.server.app.GameEventDispatcher;
import dzida.server.app.npc.AiService;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionStore;
import dzida.server.core.basic.unit.Point;

import java.util.List;

public class NpcScenarioLogic {
    private final AiService aiService;
    private final PositionStore positionStore;
    private final CommandResolver commandResolver;
    private final GameEventDispatcher gameEventDispatcher;

    public NpcScenarioLogic(AiService aiService, PositionStore positionStore, CommandResolver commandResolver, GameEventDispatcher gameEventDispatcher) {
        this.aiService = aiService;
        this.positionStore = positionStore;
        this.commandResolver = commandResolver;
        this.gameEventDispatcher = gameEventDispatcher;
    }

    public void addNpc(Point position, int npcType) {
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));
        NpcCharacter character = new NpcCharacter(characterId, npcType);
        positionStore.setPosition(characterId, position);
        aiService.createNpc(npcType, character);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, event -> {
            List<GameEvent> gameEvents = aiService.processPacket(characterId, event);
            gameEventDispatcher.dispatchEvents(gameEvents);
        });
    }

    public void removeNpc(Id<Character> characterId) {
        aiService.removeNpc(characterId);
    }
}
