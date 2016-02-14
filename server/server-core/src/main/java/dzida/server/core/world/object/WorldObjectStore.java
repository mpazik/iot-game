package dzida.server.core.world.object;

import dzida.server.core.basic.entity.GeneralEntity;

import java.util.List;

public interface WorldObjectStore {
    List<GeneralEntity<WorldObject>> getAll();

    GeneralEntity<WorldObject> createWorldObject(int objectKind, int x, int y);

    void saveObject(GeneralEntity<WorldObject> worldObject);
}
