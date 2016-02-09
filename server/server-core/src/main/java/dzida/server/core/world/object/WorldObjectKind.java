package dzida.server.core.world.object;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;


public class WorldObjectKind {
    @EqualsAndHashCode(callSuper = false)
    @Value
    @Builder(toBuilder = true)
    public static final class Data extends dzida.server.core.basic.entity.Data {
        String key;
        int width;
        int height;
        SeparateCollisionLayer separateCollisionLayer;
    }

    public static final class Id extends dzida.server.core.basic.entity.Id<WorldObjectKind.Data> {
        // to do make it private
        public Id(long id) {
            super(id);
        }
    }

    public static final class Entity extends dzida.server.core.basic.entity.Entity<WorldObjectKind.Id, WorldObjectKind.Data> {

        public Entity(Id id, WorldObjectKind.Data data) {
            super(id, data);
        }
    }

    @Value
    public static final class SeparateCollisionLayer {
       int width;
       int height;
       int offsetX;
       int offsetY;
    }
}
