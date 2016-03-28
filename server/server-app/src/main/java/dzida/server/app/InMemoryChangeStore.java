package dzida.server.app;

import dzida.server.core.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class InMemoryChangeStore implements ChangesStore {
    Map<EntityType, Map<EntityId, List<Change>>> changes = new HashMap<>();

    @Override
    public void save(ChangeDto changeDto) {
        if (!changes.containsKey(changeDto.entityType)) {
            changes.put(changeDto.entityType, new HashMap<>());
        }

        Map<EntityId, List<Change>> typeChanges = changes.get(changeDto.entityType);
        if (!typeChanges.containsKey(changeDto.entityId)) {
            typeChanges.put(changeDto.entityId, new ArrayList<>());
        }
        List<Change> entityChanges = typeChanges.get(changeDto.entityId);
        entityChanges.add(changeDto.change);
    }

    @Override
    public <T extends State<T>> Stream<Change<T>> getChanges(EntityType<T> entityType, EntityId<T> entityId) {
        //noinspection unchecked
        return changes.get(entityType).get(entityId).stream().map(change -> (Change<T>) change);
    }
}
