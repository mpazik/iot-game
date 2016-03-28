package dzida.server.core.entity;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;

public final class StateBuilderTypeRegistry {
    private final ImmutableMap<EntityType<?>, Class<?>> builderClasses;

    private StateBuilderTypeRegistry(ImmutableMap<EntityType<?>, Class<?>> builderClasses) {
        this.builderClasses = builderClasses;
    }

    public <T extends State<T>> Class<StateBuilder<T>> getStateBuilderClass(EntityType<T> entityType) {
        //noinspection unchecked
        Class<StateBuilder<T>> entityClass = (Class<StateBuilder<T>>) builderClasses.get(entityType);
        assert entityClass != null;
        return entityClass;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        HashMap<EntityType<?>, Class<?>> entityClasses = new HashMap<>();

        private Builder() {
        }

        public <T extends State<T>> Builder declareStateBuilder(EntityType<T> entityType, Class<? extends StateBuilder<T>> entityClass) {
            entityClasses.put(entityType, entityClass);
            return this;
        }

        public StateBuilderTypeRegistry build() {
            return new StateBuilderTypeRegistry(ImmutableMap.copyOf(entityClasses));
        }
    }
}
