package dzida.server.core.entity;

public class Version {
    private final int version;

    private Version(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public Version nextVersion() {
        return new Version(version + 1);
    }

    public static Version zero() {
        return new Version(0);
    }
}
