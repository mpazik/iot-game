package dzida.server.core.entity;

import java.util.List;

public class EntityChangesWithType<T extends State<T>> {
    public final EntityId<T> entityId;
    public final EntityType<T> entityType;
    public final List<Change<T>> changes;

    public EntityChangesWithType(EntityId<T> entityId, EntityType<T> entityType, List<Change<T>> changes) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.changes = changes;
    }
}
