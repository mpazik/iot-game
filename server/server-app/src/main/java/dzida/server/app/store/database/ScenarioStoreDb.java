package dzida.server.app.store.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.querydsl.core.Tuple;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.scenario.ScenarioEvent;
import dzida.server.app.instance.scenario.ScenarioEvent.ScenarioFinished;
import dzida.server.app.instance.scenario.ScenarioEvent.ScenarioStarted;
import dzida.server.app.instance.scenario.ScenarioEventBox;
import dzida.server.app.instance.scenario.ScenarioStore;
import dzida.server.app.map.descriptor.OpenWorld;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.serialization.BasicJsonSerializer;
import dzida.server.app.serialization.MessageSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.scenario.ScenarioEnd;
import org.postgresql.util.PGobject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static dzida.server.app.querydsl.QScenarioEvent.scenarioEvent;
import static dzida.server.app.serialization.BasicJsonSerializer.keyTypeAdapter;

public class ScenarioStoreDb implements ScenarioStore {
    private final ConnectionProvider connectionProvider;
    private final MessageSerializer scenarioEventSerializer;

    public ScenarioStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        Gson serializer = new GsonBuilder()
                .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
                .registerTypeHierarchyAdapter(Key.class, keyTypeAdapter)
                .registerTypeAdapter(Scenario.class, (JsonDeserializer<Scenario>) (json, typeOfT, context) -> {
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
                })
                .registerTypeAdapter(Scenario.class, (JsonSerializer<Scenario>) (src, typeOfSrc, context1) ->
                        BasicJsonSerializer.getSerializer().toJsonTree(src))
                .create();
        scenarioEventSerializer = MessageSerializer.create(serializer, ScenarioEvent.classes);
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
        String scenarioEndEventType = scenarioEventSerializer.getMessageType(ScenarioFinished.class);
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
                ScenarioEvent event = (ScenarioEvent) scenarioEventSerializer.parseEvent(data, type);
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
        String eventType = scenarioEventSerializer.getMessageType(event);
        String eventData = scenarioEventSerializer.serializeMessage(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(scenarioEvent)
                    .set(scenarioEvent.scenarioId, scenarioId.getIntValue())
                    .set(scenarioEvent.type, eventType)
                    .set(scenarioEvent.data, eventData)
                    .execute();
        });
    }
}
