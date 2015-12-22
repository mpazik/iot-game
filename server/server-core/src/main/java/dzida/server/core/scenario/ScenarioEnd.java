package dzida.server.core.scenario;

import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class ScenarioEnd implements GameEvent {
    public enum Resolution {
        Victory,
        Defeat
    }
    Resolution resolution;

    @Override
    public int getId() {
        return GameEvent.ScenarioEnd;
    }
}
