package dzida.server.core.entity;

public interface GeneralStateStore {
    <T extends State<T>> T getState(EntityId<T> entityId, EntityType<T> entityType);
}
