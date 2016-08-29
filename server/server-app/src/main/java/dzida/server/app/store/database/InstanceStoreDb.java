package dzida.server.app.store.database;

import dzida.server.app.BasicJsonSerializer;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceStore;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;

import static dzida.server.app.querydsl.QInstanceEvent.instanceEvent;

public class InstanceStoreDb implements InstanceStore {
    private final ConnectionProvider connectionProvider;

    public InstanceStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void saveEvent(Key<Instance> instanceKey, GameEvent gameEvent) {
        String data = BasicJsonSerializer.getSerializer().toJson(gameEvent);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(instanceEvent)
                    .set(instanceEvent.instanceKey, instanceKey.getValue())
                    .set(instanceEvent.data, data)
                    .execute();
        });
    }
}
