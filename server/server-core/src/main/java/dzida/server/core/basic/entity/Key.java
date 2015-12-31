package dzida.server.core.basic.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Key<T> {
    private final String key;

    public Key(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty(), "Key can not be empty");
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key<?> key1 = (Key<?>) o;
        return Objects.equal(key, key1.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return "Key{" + key + '}';
    }

    public String getValue() {
        return key;
    }
}
