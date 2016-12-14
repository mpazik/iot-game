package dzida.server.app.instance;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StateSynchroniser {
    private final Map<Id<User>, Consumer<GameEvent>> listeners = new HashMap<>();

    private final Instance instance;
    private final Scenario scenario;

    public StateSynchroniser(Instance instance, Scenario scenario) {
        this.instance = instance;
        this.scenario = scenario;
    }

    public void registerCharacter(Id<User> listenerId, Consumer<GameEvent> send) {
        listeners.put(listenerId, send);
        sendInitialPacket(listenerId);
    }

    public void unregisterListener(Id<User> listenerId) {
        listeners.remove(listenerId);
    }

    public void sendInitialPacket(Id<User> userId) {
        InitialData initialData = new InitialData(instance.getState(), scenario);
        listeners.get(userId).accept(initialData);
    }

    public void syncStateChange(GameEvent gameEvent) {
        listeners.values().forEach(consumer -> consumer.accept(gameEvent));
    }

    public static final class InitialData implements GameEvent {
        Map<String, Object> state;
        Scenario scenario;

        public InitialData(Map<String, Object> state, Scenario scenario) {
            this.state = state;
            this.scenario = scenario;
        }
    }
}
