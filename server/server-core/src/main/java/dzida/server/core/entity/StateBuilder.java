package dzida.server.core.entity;

public abstract class StateBuilder<T extends State<T>> {
    private T state;
    private Version version;

    protected StateBuilder() {
        version = Version.zero();
    }

    public abstract void applyChange(Change<T> change);

    protected void updateState(T newState) {
        state = newState;
        version = version.nextVersion();
    }

    public T getState() {
        return state;
    }

    public Version getVersion() {
        return version;
    }
}
