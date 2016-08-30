package dzida.server.app.instance.scenario;

import dzida.server.app.map.descriptor.Scenario;
import dzida.server.core.basic.entity.Id;

public class ScenarioEventBox {
    public final Id<Scenario> scenarioId;
    public final ScenarioEvent event;
    public final long createdAt;

    public ScenarioEventBox(Id<Scenario> scenarioId, ScenarioEvent event, long createdAt) {
        this.scenarioId = scenarioId;
        this.event = event;
        this.createdAt = createdAt;
    }
}
