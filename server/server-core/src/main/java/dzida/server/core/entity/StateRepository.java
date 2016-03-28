package dzida.server.core.entity;

public final class StateRepository<T extends State<T>> {
    private final GeneralStateStore generalStateStore;
    private final EntityType<T> entityType;

    public StateRepository(GeneralStateStore generalStateStore, EntityType<T> entityType) {
        this.generalStateStore = generalStateStore;
        this.entityType = entityType;
    }

    public T getState(EntityId<T> entityId) {
        return generalStateStore.getState(entityId, entityType);
    }
}
