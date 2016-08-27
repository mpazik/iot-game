package dzida.server.app.store.database;

import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.ScenarioStore;
import dzida.server.app.instance.scenario.event.ScenarioEvent;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioFinished;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioStarted;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.store.EventSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.scenario.ScenarioEnd;

import java.util.Random;

import static dzida.server.app.querydsl.QScenarioEvent.scenarioEvent;

public class ScenarioStoreDb implements ScenarioStore {
    private final ConnectionProvider connectionProvider;
    private final EventSerializer eventSerializer;

    public ScenarioStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        eventSerializer = new EventSerializer.Builder()
                .registerEvent(ScenarioStarted.class)
                .registerEvent(ScenarioFinished.class)
                .build();
    }

    @Override
    public Id<Scenario> scenarioStarted(Scenario scenario) {
        Id<Scenario> scenarioId = generateScenarioId();
        saveEvent(scenarioId, new ScenarioStarted(scenario));
        return scenarioId;
    }

    @Override
    public void scenarioFinished(Id<Scenario> scenarioId, ScenarioEnd scenarioEnd) {
        saveEvent(scenarioId, new ScenarioFinished(scenarioEnd.resolution));
    }

    @Override
    public boolean isScenarioFinished(Id<Scenario> scenarioId) {
        String scenarioEndEventType = eventSerializer.getEventType(ScenarioFinished.class);
        return connectionProvider.withSqlFactory(sqlQueryFactory -> {
            long scenarioEndCount = sqlQueryFactory.select()
                    .from(scenarioEvent)
                    .limit(1)
                    .where(scenarioEvent.scenarioId.eq(scenarioId.getIntValue())
                            .and(scenarioEvent.type.eq(scenarioEndEventType)))
                    .fetchCount();
            return scenarioEndCount == 1;
        });
    }

    private Id<Scenario> generateScenarioId() {
        return new Id<>(new Random().nextInt(100000000));
    }

    private void saveEvent(Id<Scenario> scenarioId, ScenarioEvent event) {
        String eventType = eventSerializer.getEventType(event);
        String eventData = eventSerializer.serializeEvent(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(scenarioEvent)
                    .set(scenarioEvent.scenarioId, scenarioId.getIntValue())
                    .set(scenarioEvent.type, eventType)
                    .set(scenarioEvent.data, eventData)
                    .execute();
        });
    }
}
