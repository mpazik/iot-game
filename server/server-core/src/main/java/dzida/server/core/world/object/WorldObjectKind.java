package dzida.server.core.world.object;

import dzida.server.core.basic.entity.GeneralData;
import dzida.server.core.basic.entity.Id;

import java.util.Objects;

public class WorldObjectKind implements GeneralData<WorldObjectKind> {
    private final Id<WorldObjectKind> id;
    private final String key;
    private final int width;
    private final int height;
    private final CollisionLayer collisionLayer;

    public WorldObjectKind(Id<WorldObjectKind> id, String key, int width, int height, CollisionLayer collisionLayer) {
        this.id = id;
        this.key = key;
        this.width = width;
        this.height = height;
        this.collisionLayer = collisionLayer;
    }

    public Id<WorldObjectKind> getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public CollisionLayer getCollisionLayer() {
        return collisionLayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorldObjectKind that = (WorldObjectKind) o;
        return width == that.width &&
                height == that.height &&
                Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(collisionLayer, that.collisionLayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, width, height, collisionLayer);
    }

    public static final class CollisionLayer {
        private final int width;
        private final int height;
        private final int offsetX;
        private final int offsetY;

        public CollisionLayer(int width, int height, int offsetX, int offsetY) {
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }
}
