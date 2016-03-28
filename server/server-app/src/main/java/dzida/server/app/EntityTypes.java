package dzida.server.app;

import dzida.server.core.abilities.Abilities;
import dzida.server.core.entity.EntityType;

public final class EntityTypes {
    private EntityTypes() {
    }

    public static final EntityType<Abilities> abilities = new EntityType<>(0);
}
