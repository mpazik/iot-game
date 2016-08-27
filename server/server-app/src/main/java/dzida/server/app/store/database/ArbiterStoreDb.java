package dzida.server.app.store.database;

import dzida.server.app.arbiter.ArbiterStore;
import dzida.server.app.arbiter.event.ArbiterEvent;
import dzida.server.app.arbiter.event.InstanceClosed;
import dzida.server.app.arbiter.event.InstanceStarted;
import dzida.server.app.arbiter.event.SystemClosed;
import dzida.server.app.arbiter.event.SystemStarted;
import dzida.server.app.arbiter.event.UserJoinedInstance;
import dzida.server.app.arbiter.event.UserLeftInstance;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.store.EventSerializer;
import dzida.server.app.user.User;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;

import java.util.Random;

import static dzida.server.app.querydsl.QArbiterEvent.arbiterEvent;

public class ArbiterStoreDb implements ArbiterStore {
    private final ConnectionProvider connectionProvider;
    private final EventSerializer eventSerializer;

    public ArbiterStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        eventSerializer = new EventSerializer.Builder()
                .registerEvent(SystemStarted.class)
                .registerEvent(SystemClosed.class)
                .registerEvent(InstanceStarted.class)
                .registerEvent(InstanceClosed.class)
                .registerEvent(UserJoinedInstance.class)
                .registerEvent(UserLeftInstance.class)
                .build();
    }

    @Override
    public void systemStarted() {
        saveEvent(new SystemStarted());
    }

    @Override
    public void systemStopped() {
        saveEvent(new SystemClosed());
    }

    @Override
    public Key<Instance> createInstance() {
        return generateInstanceKey();
    }

    @Override
    public void instanceStarted(Key<Instance> instanceKey) {
        saveEvent(new InstanceStarted(instanceKey));
    }

    @Override
    public void instanceStopped(Key<Instance> instanceKey) {
        saveEvent(new InstanceClosed(instanceKey));
    }

    @Override
    public void userJoinedInstance(Id<User> userId, Key<Instance> instanceKey) {
        saveEvent(new UserJoinedInstance(instanceKey, userId));
    }

    @Override
    public void playerLeftInstance(Id<User> userId, Key<Instance> instanceKey) {
        saveEvent(new UserLeftInstance(instanceKey, userId));
    }

    private Key<Instance> generateInstanceKey() {
        return new Key<>("inst" + new Random().nextInt(1000000));
    }

    private void saveEvent(ArbiterEvent event) {
        String eventType = eventSerializer.getEventType(event);
        String eventData = eventSerializer.serializeEvent(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(arbiterEvent)
                    .set(arbiterEvent.type, eventType)
                    .set(arbiterEvent.data, eventData)
                    .execute();
        });
    }
}
