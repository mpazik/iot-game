package dzida.server.core.world.object;

import com.google.common.base.Objects;
import dzida.server.core.basic.entity.GeneralData;
import dzida.server.core.basic.entity.Id;

import java.time.Instant;


public final class WorldObject implements GeneralData<WorldObject> {
        private final Id<WorldObjectKind> kind;
        private final int x;
        private final int y;
    private final Instant created;

    public WorldObject(Id<WorldObjectKind> kind, int x, int y, Instant created) {
                this.kind = kind;
                this.x = x;
                this.y = y;
        this.created = created;
        }

        public Id<WorldObjectKind> getKind() {
                return kind;
        }

        public int getX() {
                return x;
        }

        public int getY() {
                return y;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                WorldObject that = (WorldObject) o;
                return x == that.x &&
                        y == that.y &&
                        Objects.equal(kind, that.kind);
        }

        @Override
        public int hashCode() {
                return Objects.hashCode(kind, x, y);
        }
}
