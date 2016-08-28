package dzida.server.app.store.database;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.querydsl.core.Tuple;
import dzida.server.app.BasicJsonSerializer;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.scenario.ScenarioEventBox;
import dzida.server.app.instance.scenario.ScenarioStore;
import dzida.server.app.instance.scenario.event.ScenarioEvent;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioFinished;
import dzida.server.app.instance.scenario.event.ScenarioEvent.ScenarioStarted;
import dzida.server.app.map.descriptor.OpenWorld;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.store.EventSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.scenario.ScenarioEnd;
import org.postgresql.util.PGobject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static dzida.server.app.querydsl.QScenarioEvent.scenarioEvent;

public class ScenarioStoreDb implements ScenarioStore {
    public static final JsonDeserializer<Scenario> scenarioDeserializer = (json, typeOfT, context) -> {
        JsonObject scenario = json.getAsJsonObject();
        String type = scenario.get("type").getAsString();
        switch (type) {
            case "open-world":
                return BasicJsonSerializer.getSerializer().fromJson(scenario, OpenWorld.class);
            case "survival":
                return BasicJsonSerializer.getSerializer().fromJson(scenario, Survival.class);
            default:
                throw new RuntimeException("can not parse scenario of type: " + type);
        }
    };
    public static final JsonSerializer<Scenario> scenarioSerializer = (src, typeOfSrc, context) ->
            BasicJsonSerializer.getSerializer().toJsonTree(src);
    private final ConnectionProvider connectionProvider;
    private final EventSerializer eventSerializer;

    public ScenarioStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        eventSerializer = new EventSerializer.Builder()
                .setSerializer(new GsonBuilder()
                        .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                        .registerTypeHierarchyAdapter(Key.class, BasicJsonSerializer.keyTypeAdapter)
                        .registerTypeAdapter(Scenario.class, scenarioDeserializer)
                        .registerTypeAdapter(Scenario.class, scenarioSerializer)
                        .create())
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

    @Override
    public List<ScenarioEventBox> getEvents(long timestamp) {
        return connectionProvider.withSqlFactory(sqlQueryFactory -> {
            List<Tuple> fetch = sqlQueryFactory.select(scenarioEvent.scenarioId, scenarioEvent.type, scenarioEvent.data, scenarioEvent.createdAt)
                    .from(scenarioEvent)
                    .where(scenarioEvent.createdAt.goe(new Timestamp(timestamp)))
                    .fetch();
            return fetch.stream().map(tuple -> {
                PGobject pGobject = (PGobject) tuple.get(scenarioEvent.data);
                assert pGobject != null;
                String data = pGobject.getValue();
                String type = tuple.get(scenarioEvent.type);
                ScenarioEvent event = (ScenarioEvent) eventSerializer.parseEvent(data, type);
                Integer id = tuple.get(scenarioEvent.scenarioId);
                assert id != null;
                Id<Scenario> scenarioId = new Id<>(id);
                Timestamp createdAt = tuple.get(scenarioEvent.createdAt);
                assert createdAt != null;
                return new ScenarioEventBox(scenarioId, event, createdAt.getTime());
            }).collect(Collectors.toList());
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
