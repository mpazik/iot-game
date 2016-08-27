package dzida.server.app.instance.scenario.event;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.scenario.ScenarioEnd;

public interface ScenarioEvent {

    class ScenarioStarted implements ScenarioEvent {
        public final Scenario scenario;

        public ScenarioStarted(Scenario scenario) {
            this.scenario = scenario;
        }
    }

    class ScenarioFinished implements ScenarioEvent {
        public final ScenarioEnd.Resolution resolution;

        public ScenarioFinished(ScenarioEnd.Resolution resolution) {
            this.resolution = resolution;
        }
    }
}
