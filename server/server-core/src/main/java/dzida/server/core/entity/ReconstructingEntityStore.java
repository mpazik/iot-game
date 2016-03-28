package dzida.server.core.entity;

import java.util.stream.Stream;

public class ReconstructingEntityStore implements GeneralStateStore {
    private final StateBuilderFactory stateBuilderFactory;
    private final ChangesStore changesStore;

    public ReconstructingEntityStore(StateBuilderTypeRegistry stateBuilderTypeRegistry, ChangesStore changesStore) {
        this.stateBuilderFactory = new StateBuilderFactory(stateBuilderTypeRegistry);
        this.changesStore = changesStore;
    }

    @Override
    public <T extends State<T>> T getState(EntityId<T> entityId, EntityType<T> entityType) {
        StateBuilder<T> stateBuilder = stateBuilderFactory.getStateBuilder(entityType);
        Stream<Change<T>> changes = changesStore.getChanges(entityType, entityId);
        changes.forEach(stateBuilder::applyChange);
        return stateBuilder.getState();
    }
}
