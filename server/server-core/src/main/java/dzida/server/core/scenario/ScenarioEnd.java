package dzida.server.core.scenario;

import dzida.server.core.event.GameEvent;

public class ScenarioEnd implements GameEvent {
    public enum Resolution {
        Victory,
        Defeat
    }

    public final Resolution resolution;

    public ScenarioEnd(Resolution resolution) {
        this.resolution = resolution;
    }

    @Override
    public int getId() {
        return GameEvent.ScenarioEnd;
    }
}
