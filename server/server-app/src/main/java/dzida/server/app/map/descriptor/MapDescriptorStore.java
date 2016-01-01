package dzida.server.app.map.descriptor;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.position.model.Point;

import java.util.Optional;

public class MapDescriptorStore {

    public Optional<Scenario> getDescriptor(String name, Integer difficultyLevel) {
        if ("eden".equals(name)) {
            return Optional.of(new OpenWorld(new Key<>("eden")));
        }
        if ("small-island".equals(name)){
            return Optional.of(new Survival(new Key<>("small-island"), difficultyLevel, ImmutableList.of(
                    new Survival.Spawn(new Point(19, 10)),
                    new Survival.Spawn(new Point(19, 14))
            )));
        }
        return Optional.empty();
    }
}
