package dzida.server.app.instance;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StateSynchroniser {
    private final Map<Id<Player>, Consumer<GameEvent>> listeners = new HashMap<>();

    private final Instance instance;
    private final Scenario scenario;

    public StateSynchroniser(Instance instance, Scenario scenario) {
        this.instance = instance;
        this.scenario = scenario;
    }

    public void registerCharacter(Id<Player> listenerId, Consumer<GameEvent> send) {
        listeners.put(listenerId, send);
    }

    public void unregisterListener(Id<Player> listenerId) {
        listeners.remove(listenerId);
    }


    public void sendInitialPacket(Id<Character> characterId, Id<Player> playerId, Player playerEntity) {
        InitialMessage initialMessage = new InitialMessage(characterId, playerId, instance.getState(), scenario, playerEntity.getData());
        listeners.get(playerId).accept(initialMessage);
    }

    public void syncStateChange(GameEvent gameEvent) {
        listeners.values().forEach(consumer -> consumer.accept(gameEvent));
    }

    public static final class InitialMessage implements GameEvent {
        Id<Character> characterId;
        Id<Player> playerId;
        Map<String, Object> state;
        Scenario scenario;
        Player.Data playerData;

        public InitialMessage(Id<Character> characterId, Id<Player> playerId, Map<String, Object> state, Scenario scenario, dzida.server.core.player.Player.Data playerData) {
            this.characterId = characterId;
            this.playerId = playerId;
            this.state = state;
            this.scenario = scenario;
            this.playerData = playerData;
        }
    }
}
