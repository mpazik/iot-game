package dzida.server.core.world.object;

import dzida.server.core.basic.entity.GeneralData;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
@Builder(toBuilder = true)
public class WorldObjectKind implements GeneralData<WorldObjectKind> {
    String key;
    int width;
    int height;
    SeparateCollisionLayer separateCollisionLayer;

    @Value
    public static final class SeparateCollisionLayer {
        int width;
        int height;
        int offsetX;
        int offsetY;
    }
}
