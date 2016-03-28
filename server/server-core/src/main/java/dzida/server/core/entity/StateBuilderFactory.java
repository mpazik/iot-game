package dzida.server.core.entity;

public final class StateBuilderFactory {
    private final StateBuilderTypeRegistry stateBuilderTypeRegistry;

    public StateBuilderFactory(StateBuilderTypeRegistry stateBuilderTypeRegistry) {
        this.stateBuilderTypeRegistry = stateBuilderTypeRegistry;
    }

    public <T extends State<T>> StateBuilder<T> getStateBuilder(EntityType<T> entityType) {
        Class<StateBuilder<T>> entityClass = stateBuilderTypeRegistry.getStateBuilderClass(entityType);
        try {
            return entityClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
