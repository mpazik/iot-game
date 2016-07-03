package dzida.server.core.world.object;

import dzida.server.core.basic.entity.GeneralData;

import java.util.Objects;

public class WorldObjectKind implements GeneralData<WorldObjectKind> {
    String key;
    int width;
    int height;
    SeparateCollisionLayer separateCollisionLayer;

    public WorldObjectKind(String key, int width, int height, SeparateCollisionLayer separateCollisionLayer) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.separateCollisionLayer = separateCollisionLayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldObjectKind that = (WorldObjectKind) o;
        return width == that.width &&
                height == that.height &&
                Objects.equals(key, that.key) &&
                Objects.equals(separateCollisionLayer, that.separateCollisionLayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, width, height, separateCollisionLayer);
    }

    public static final class SeparateCollisionLayer {
        int width;
        int height;
        int offsetX;
        int offsetY;
    }
}
