package dzida.server.app.store.mapdb;

import dzida.server.app.Serializer;
import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectKind;
import dzida.server.core.world.object.WorldObjectStore;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

public class WorldObjectStoreMapDb implements WorldObjectStore {
    long idGenerator = 0;
    private final Serializer serializer;
    private final ConcurrentNavigableMap<Long, String> worldObjects;

    public WorldObjectStoreMapDb(Serializer serializer, ConcurrentNavigableMap<Long, String> table) {
        this.serializer = serializer;
        worldObjects = table;
        idGenerator = getLastIdFromDb();
    }

    @Override
    public List<GeneralEntity<WorldObject>> getAll() {
        return worldObjects.entrySet().stream()
                .map(entry -> {
                    WorldObject data = serializer.fromJson(entry.getValue(), WorldObject.class);
                    Id<WorldObject> id = new Id<>(entry.getKey());
                    return new GeneralEntity<>(id, data);
                }).collect(Collectors.toList());
    }

    @Override
    public GeneralEntity<WorldObject> createWorldObject(int objectKind, int x, int y) {
        Id<WorldObjectKind> id = new Id<>((long) objectKind);
        WorldObject worldObject = new WorldObject(id, x, y);
        return new GeneralEntity<>(
                new Id<>(newId()),
                worldObject
        );
    }

    @Override
    public void saveObject(GeneralEntity<WorldObject> worldObject) {
        worldObjects.put(worldObject.getId().getValue(), serializer.toJson(worldObject.getData()));
    }

    @Override
    public void removeObject(Id<WorldObject> worldObjectId) {
        worldObjects.remove(worldObjectId.getValue());
    }

    private Long getLastIdFromDb() {
        NavigableSet<Long> keySet = worldObjects.keySet();
        if (keySet.isEmpty()) {
            return 1L;
        }
        return keySet.last() + 1;
    }

    private Long newId() {
        return ++idGenerator;
    }
}
