package dzida.server.core.entity;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class EntityChanges<T extends State<T>> {
    public final EntityId<T> entityId;
    public final List<Change<T>> changes;

    public EntityChanges(EntityId<T> entityId, List<Change<T>> changes) {
        this.entityId = entityId;
        this.changes = changes;
    }

    public static <T extends State<T>> EntityChanges<T> change(EntityId<T> entityId, Change<T> changes) {
        return new EntityChanges<>(entityId, ImmutableList.of(changes));
    }
}
