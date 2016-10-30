package dzida.server.app.store.memory;

import com.google.gson.Gson;
import dzida.server.app.serialization.BasicJsonSerializer;
import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectKind;
import dzida.server.core.world.object.WorldObjectStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldObjectStoreInMemory implements WorldObjectStore {
    private final Gson serializer = BasicJsonSerializer.getSerializer();
    private final Map<Long, String> worldObjects;
    long idGenerator = 0;

    public WorldObjectStoreInMemory() {
        worldObjects = new HashMap<>();
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
    public GeneralEntity<WorldObject> createWorldObject(Id<WorldObjectKind> objectKindId, int x, int y) {
        WorldObject worldObject = new WorldObject(objectKindId, x, y);
        return new GeneralEntity<>(
                new Id<>(newId()),
                worldObject
        );
    }

    @Override
    public void createObject(GeneralEntity<WorldObject> worldObject) {
        worldObjects.put(worldObject.getId().getValue(), serializer.toJson(worldObject.getData()));
    }

    @Override
    public void removeObject(Id<WorldObject> worldObjectId) {
        worldObjects.remove(worldObjectId.getValue());
    }

    private Long newId() {
        return ++idGenerator;
    }
}
