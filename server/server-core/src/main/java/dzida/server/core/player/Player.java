package dzida.server.core.player;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;


public class Player {

    @EqualsAndHashCode(callSuper = false)
    @Value
    @Builder(toBuilder = true)
    public static final class Data extends dzida.server.core.basic.entity.Data {
        String nick;
        int highestDifficultyLevel;
        int lastDifficultyLevel;
    }


    public static final class Id extends dzida.server.core.basic.entity.Id<Player.Data> {
        // to do make it private
        public Id(long id) {
            super(id);
        }
    }

    public static final class Entity extends dzida.server.core.basic.entity.Entity<Player.Id, Player.Data> {

        public Entity(Id id, Player.Data data) {
            super(id, data);
        }
    }
}
