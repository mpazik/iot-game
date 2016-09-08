package dzida.server.core.basic.entity;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;

public class Key<T> {
    private final String key;

    public Key(@NotNull String key) {
        assert key != null;
        assert key.length() > 0 : "key can not be empty";
        assert key.matches("^.*[^.-a-zA-Z0-9 ].*$") : "key can contain only alphanumeric characters, dot and dash";
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

    @NotNull
    public String getValue() {
        return key;
    }
}
