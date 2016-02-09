package dzida.server.core.world.object;

import lombok.EqualsAndHashCode;
import lombok.Value;


public class WorldObject {
    @EqualsAndHashCode(callSuper = false)
    @Value
    public static final class Data extends dzida.server.core.basic.entity.Data {
        WorldObjectKind.Id kind;
        int x;
        int y;
    }


    public static final class Id extends dzida.server.core.basic.entity.Id<WorldObject.Data> {
        // to do make it private
        public Id(long id) {
            super(id);
        }
    }

    public static final class Entity extends dzida.server.core.basic.entity.Entity<WorldObject.Id, WorldObject.Data> {

        public Entity(Id id, WorldObject.Data data) {
            super(id, data);
        }
    }
}
