package dzida.server.app.store.database;

import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceStore;
import dzida.server.app.instance.Serialization;
import dzida.server.app.serialization.MessageSerializer;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;

import static dzida.server.app.querydsl.QInstanceEvent.instanceEvent;

public class InstanceStoreDb implements InstanceStore {
    private final ConnectionProvider connectionProvider;
    private final MessageSerializer instanceEventSerializer;

    public InstanceStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        instanceEventSerializer = Serialization.createInstanceEventSerializer();
    }

    @Override
    public void saveEvent(Key<Instance> instanceKey, GameEvent event) {
        String eventType = instanceEventSerializer.getEventType(event);
        String eventData = instanceEventSerializer.serializeEvent(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(instanceEvent)
                    .set(instanceEvent.instanceKey, instanceKey.getValue())
                    .set(instanceEvent.type, eventType)
                    .set(instanceEvent.data, eventData)
                    .execute();
        });
    }
}
