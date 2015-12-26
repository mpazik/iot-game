package dzida.server.core.basic.entity;

import com.google.common.base.Objects;

public abstract class Entity<I extends Id<T>, T> {
    private final I id;
    private final T data;

    public Entity(I id, T data) {
        this.id = id;
        this.data = data;
    }

    public I getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?, ?> entity = (Entity<?, ?>) o;
        return Objects.equal(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", data=" + data +
                '}';
    }
}
