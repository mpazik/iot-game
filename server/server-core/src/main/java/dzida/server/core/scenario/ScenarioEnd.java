package dzida.server.core.scenario;

import dzida.server.core.event.GameEvent;

public class ScenarioEnd implements GameEvent {
    public final Resolution resolution;
    public final int difficultyLevel;

    public ScenarioEnd(Resolution resolution, int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        this.resolution = resolution;
    }

    public enum Resolution {
        Victory,
        Defeat
    }
}
