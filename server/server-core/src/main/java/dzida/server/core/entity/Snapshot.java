package dzida.server.core.entity;

public final class Snapshot<T extends State<T>> {
    private final EntityId<T> entityId;
    private final Version version;
    private final T state;
    private final EntityType<T> type;

    public Snapshot(EntityId<T> entityId, Version version, T state, EntityType<T> type) {
        this.entityId = entityId;
        this.version = version;
        this.state = state;
        this.type = type;
    }

    public EntityId<T> getEntityId() {
        return entityId;
    }

    public Version getVersion() {
        return version;
    }

    public T getState() {
        return state;
    }

    public EntityType<T> getType() {
        return type;
    }
}
