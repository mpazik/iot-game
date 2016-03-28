package dzida.server.core.entity;

import com.google.common.base.Objects;

public class EntityId<T extends State<T>> {
    private final long id;

    public EntityId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId<?> id1 = (EntityId<?>) o;
        return id == id1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Id{" + id + '}';
    }

    public long getValue() {
        return id;
    }
}
