package dzida.server.app.instance;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.scenario.ScenarioEnd;

public interface ScenarioStore {
    Id<Scenario> scenarioStarted(Scenario scenario);

    void scenarioFinished(Id<Scenario> scenarioId, ScenarioEnd scenarioEnd);

    boolean isScenarioFinished(Id<Scenario> scenarioId);
}
