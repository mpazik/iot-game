package dzida.server.core.entity;

import java.util.stream.Stream;

public interface ChangesStore {
    void save(ChangeDto changeDto);

    <T extends State<T>> Stream<Change<T>> getChanges(EntityType<T> entityType, EntityId<T> entityId);
}
