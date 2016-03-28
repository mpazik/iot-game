package dzida.server.core.entity;

import java.util.Objects;

public class EntityType<T extends State<T>> {
    private final int value;

    public EntityType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType<?> that = (EntityType<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
