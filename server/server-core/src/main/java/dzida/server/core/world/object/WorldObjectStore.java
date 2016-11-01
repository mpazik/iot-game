package dzida.server.core.world.object;

import dzida.server.core.basic.entity.GeneralEntity;
import dzida.server.core.basic.entity.Id;

import java.util.List;

public interface WorldObjectStore {
    List<GeneralEntity<WorldObject>> getAll();

    GeneralEntity<WorldObject> getWorldObject(Id<WorldObject> id);

    GeneralEntity<WorldObject> createWorldObject(Id<WorldObjectKind> objectKindId, int x, int y);

    WorldObjectKind getWorldObjectKind(Id<WorldObjectKind> id);

    void createObject(GeneralEntity<WorldObject> worldObject);

    void removeObject(Id<WorldObject> worldObjectId);
}
