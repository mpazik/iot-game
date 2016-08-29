package dzida.server.app.store.database;

import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceProtocol;
import dzida.server.app.instance.InstanceStore;
import dzida.server.app.instance.StateSynchroniser;
import dzida.server.app.store.EventSerializer;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.position.event.CharacterMoved;
import dzida.server.core.scenario.ScenarioEnd;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.world.event.WorldObjectCreated;
import dzida.server.core.world.event.WorldObjectRemoved;

import static dzida.server.app.querydsl.QInstanceEvent.instanceEvent;

public class InstanceStoreDb implements InstanceStore {
    private final ConnectionProvider connectionProvider;
    private final EventSerializer eventSerializer;

    public InstanceStoreDb(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.eventSerializer = new EventSerializer.Builder()
                .registerEvent(CharacterSpawned.class)
                .registerEvent(CharacterDied.class)
                .registerEvent(CharacterMoved.class)
                .registerEvent(SkillUsedOnCharacter.class)
                .registerEvent(CharacterGotDamage.class)
                .registerEvent(StateSynchroniser.InitialMessage.class)
                .registerEvent(ServerMessage.class)
                .registerEvent(ScenarioEnd.class)
                .registerEvent(SkillUsedOnWorldMap.class)
                .registerEvent(WorldObjectCreated.class)
                .registerEvent(SkillUsedOnWorldObject.class)
                .registerEvent(WorldObjectRemoved.class)
                .registerEvent(InstanceProtocol.UserCharacterMessage.class)
                .build();
    }

    @Override
    public void saveEvent(Key<Instance> instanceKey, GameEvent event) {
        String eventType = eventSerializer.getEventType(event);
        String eventData = eventSerializer.serializeEvent(event);

        connectionProvider.withSqlFactory(sqlQueryFactory -> {
            sqlQueryFactory.insert(instanceEvent)
                    .set(instanceEvent.instanceKey, instanceKey.getValue())
                    .set(instanceEvent.type, eventType)
                    .set(instanceEvent.data, eventData)
                    .execute();
        });
    }
}
