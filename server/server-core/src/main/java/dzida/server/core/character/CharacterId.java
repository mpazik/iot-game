package dzida.server.core.character;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public final class CharacterId {
    private final int value;

    public CharacterId(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Id<Character> id() {
        return new Id<>(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharacterId that = (CharacterId) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return "CharacterId{" +
                "value=" + value +
                '}';
    }
}
