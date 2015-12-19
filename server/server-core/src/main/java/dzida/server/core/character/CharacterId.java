package dzida.server.core.character;

public final class CharacterId {
    private final int id;

    public CharacterId(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharacterId that = (CharacterId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "CharacterId{" +
                "id=" + id +
                '}';
    }
}
