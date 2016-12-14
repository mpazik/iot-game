package dzida.server.app.instance.world.object;

import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.world.event.WorldObjectCreated;
import dzida.server.app.instance.world.event.WorldObjectRemoved;

import java.util.List;
import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class WorldObjectService {
    public static final String Key = "worldObject";
    private final WorldObjectStore worldObjectStore;

    public WorldObjectService(WorldObjectStore worldObjectStore) {
        this.worldObjectStore = worldObjectStore;
    }

    static public WorldObjectService create(WorldObjectStore worldObjectStore) {
        return new WorldObjectService(worldObjectStore);
    }

    public List<GeneralEntity<WorldObject>> getState() {
        return worldObjectStore.getAll();
    }

    public String getKey() {
        return Key;
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent)
                .is(WorldObjectCreated.class).then(event -> worldObjectStore.createObject(event.worldObject))
                .is(WorldObjectRemoved.class).then(event -> worldObjectStore.removeObject(event.worldObject.getId()));
    }

    public GeneralEntity<WorldObject> getObject(Id<WorldObject> worldObjectId) {
        return worldObjectStore.getWorldObject(worldObjectId);
    }

    public Optional<GeneralEntity<WorldObject>> createWorldObject(Id<WorldObjectKind> objectKind, int x, int y) {
        GeneralEntity<WorldObject> worldObject = worldObjectStore.createWorldObject(objectKind, x, y);
        return Optional.of(worldObject);
    }

    public WorldObjectKind getObjectKind(Id<WorldObjectKind> objectKindId) {
        return worldObjectStore.getWorldObjectKind(objectKindId);
    }
}
