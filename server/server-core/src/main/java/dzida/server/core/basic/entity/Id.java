package dzida.server.core.basic.entity;

import com.google.common.base.Objects;

@SuppressWarnings("ALL")
public class Id<T> {
    private final long id;

    public Id(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id<?> id1 = (Id<?>) o;
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

    public int getIntValue() {
        return Math.toIntExact(id);
    }
}
