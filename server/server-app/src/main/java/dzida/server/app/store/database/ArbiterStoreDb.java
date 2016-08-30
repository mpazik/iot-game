package dzida.server.app.store.database;

import dzida.server.app.arbiter.ArbiterEvent;
import dzida.server.app.arbiter.ArbiterStore;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.serialization.MessageSerializer;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

import java.util.Random;

import static dzida.server.app.querydsl.QArbiterEvent.arbiterEvent;

public class ArbiterStoreDb implements ArbiterStore {
    private final ConnectionProvider connectionProvider;
    private final MessageSerializer arbiterEventSerializer;

    public ArbiterStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        arbiterEventSerializer = MessageSerializer.create(ArbiterEvent.classes);
    }

    @Override
    public void systemStarted() {
        saveEvent(new ArbiterEvent.SystemStarted());
    }

    @Override
    public void systemStopped() {
        saveEvent(new ArbiterEvent.SystemClosed());
    }

    @Override
    public Key<Instance> createInstance() {
        return generateInstanceKey();
    }

    @Override
    public void instanceStarted(Key<Instance> instanceKey) {
        saveEvent(new ArbiterEvent.InstanceStarted(instanceKey));
    }

    @Override
    public void instanceStopped(Key<Instance> instanceKey) {
        saveEvent(new ArbiterEvent.InstanceClosed(instanceKey));
    }

    @Override
    public void userJoinedInstance(Id<User> userId, Key<Instance> instanceKey) {
        saveEvent(new ArbiterEvent.UserJoinedInstance(instanceKey, userId));
    }

    @Override
    public void playerLeftInstance(Id<User> userId, Key<Instance> instanceKey) {
        saveEvent(new ArbiterEvent.UserLeftInstance(instanceKey, userId));
    }

    private Key<Instance> generateInstanceKey() {
        return new Key<>("inst" + new Random().nextInt(1000000));
    }

    private void saveEvent(ArbiterEvent event) {
        String eventType = arbiterEventSerializer.getMessageType(event);
        String eventData = arbiterEventSerializer.serializeMessage(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(arbiterEvent)
                    .set(arbiterEvent.type, eventType)
                    .set(arbiterEvent.data, eventData)
                    .execute();
        });
    }
}
