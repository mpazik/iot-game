package dzida.server.core.scenario;

import dzida.server.core.event.GameEvent;

public class ScenarioEnd implements GameEvent {
    public final Resolution resolution;

    public ScenarioEnd(Resolution resolution) {
        this.resolution = resolution;
    }

    public enum Resolution {
        Victory,
        Defeat,
        Terminated
    }
}
