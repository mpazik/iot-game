package dzida.server.app.store.database;

import dzida.server.app.basic.entity.Key;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceEvent;
import dzida.server.app.instance.InstanceStore;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.serialization.MessageSerializer;

import static dzida.server.app.querydsl.QInstanceEvent.instanceEvent;

public class InstanceStoreDb implements InstanceStore {
    private final ConnectionProvider connectionProvider;
    private final MessageSerializer instanceEventSerializer;

    public InstanceStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        instanceEventSerializer = MessageSerializer.create(InstanceEvent.classes);
    }

    @Override
    public void saveEvent(Key<Instance> instanceKey, GameEvent event) {
        String eventType = instanceEventSerializer.getMessageType(event);
        String eventData = instanceEventSerializer.serializeMessage(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(instanceEvent)
                    .set(instanceEvent.instanceKey, instanceKey.getValue())
                    .set(instanceEvent.type, eventType)
                    .set(instanceEvent.data, eventData)
                    .execute();
        });
    }
}
