package dzida.server.app.instance;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.basic.entity.Id;
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
        sendInitialPacket(listenerId);
    }

    public void unregisterListener(Id<Player> listenerId) {
        listeners.remove(listenerId);
    }


    public void sendInitialPacket(Id<Player> playerId) {
        InitialMessage initialMessage = new InitialMessage(instance.getState(), scenario);
        listeners.get(playerId).accept(initialMessage);
    }

    public void syncStateChange(GameEvent gameEvent) {
        listeners.values().forEach(consumer -> consumer.accept(gameEvent));
    }

    public static final class InitialMessage implements GameEvent {
        Map<String, Object> state;
        Scenario scenario;

        public InitialMessage(Map<String, Object> state, Scenario scenario) {
            this.state = state;
            this.scenario = scenario;
        }
    }
}
