package dzida.server.app.store.mapdb;

import com.google.common.collect.ImmutableList;
import dzida.server.core.world.object.WorldObject;
import dzida.server.core.world.object.WorldObjectKind;
import dzida.server.core.world.object.WorldObjectStore;

import java.util.List;

public class WorldObjectStoreMapDb implements WorldObjectStore {
    List<WorldObject.Entity> entities = ImmutableList.of(
            new WorldObject.Entity(
                    new WorldObject.Id(1),
                    new WorldObject.Data(new WorldObjectKind.Id(1), 25, 40)
            )
    );

    @Override
    public List<WorldObject.Entity> getAll() {
        return entities;
    }
}
