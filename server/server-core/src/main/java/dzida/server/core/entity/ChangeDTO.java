package dzida.server.core.entity;

public final class ChangeDto<T extends State<T>> {
    public final Change change;
    public final EntityId<T> entityId;
    public final EntityType<T> entityType;

    public ChangeDto(Change change, EntityId<T> entityId, EntityType<T> entityType) {
        this.change = change;
        this.entityId = entityId;
        this.entityType = entityType;
    }

}

