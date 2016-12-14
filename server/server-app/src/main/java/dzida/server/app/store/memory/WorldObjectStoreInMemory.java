package dzida.server.app.store.memory;

import com.google.common.collect.ImmutableList;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;
import dzida.server.app.instance.world.object.WorldObjectStore;
import dzida.server.app.time.TimeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldObjectStoreInMemory implements WorldObjectStore {
    private final Map<Id<WorldObject>, GeneralEntity<WorldObject>> worldObjects;
    private final Map<Id<WorldObjectKind>, WorldObjectKind> worldObjectKinds;
    private final TimeService timeService;
    long idGenerator = 0;

    public WorldObjectStoreInMemory(List<WorldObjectKind> worldObjectKinds, TimeService timeService) {
        this.timeService = timeService;
        worldObjects = new HashMap<>();
        this.worldObjectKinds = worldObjectKinds.stream().collect(Collectors.toMap(WorldObjectKind::getId, worldObjectKind -> worldObjectKind));
    }

    @Override
    public List<GeneralEntity<WorldObject>> getAll() {
        return ImmutableList.copyOf(worldObjects.values());
    }

    @Override
    public GeneralEntity<WorldObject> getWorldObject(Id<WorldObject> id) {
        return worldObjects.get(id);
    }

    @Override
    public GeneralEntity<WorldObject> createWorldObject(Id<WorldObjectKind> objectKindId, int x, int y) {
        WorldObject worldObject = new WorldObject(objectKindId, x, y, timeService.getCurrentTime());
        return new GeneralEntity<>(
                new Id<>(newId()),
                worldObject
        );
    }

    @Override
    public WorldObjectKind getWorldObjectKind(Id<WorldObjectKind> id) {
        return worldObjectKinds.get(id);
    }

    @Override
    public void createObject(GeneralEntity<WorldObject> worldObject) {
        worldObjects.put(worldObject.getId(), worldObject);
    }

    @Override
    public void removeObject(Id<WorldObject> worldObjectId) {
        worldObjects.remove(worldObjectId);
    }

    private Long newId() {
        return ++idGenerator;
    }
}
