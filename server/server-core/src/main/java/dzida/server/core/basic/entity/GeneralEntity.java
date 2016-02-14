package dzida.server.core.basic.entity;

import com.google.common.base.Objects;

public final class GeneralEntity<T extends GeneralData<T>> {
    private final Id<T> id;
    private final T data;

    public GeneralEntity(Id<T> id, T data) {
        this.id = id;
        this.data = data;
    }

    public Id<T> getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneralEntity<?> entity = (GeneralEntity<?>) o;
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
