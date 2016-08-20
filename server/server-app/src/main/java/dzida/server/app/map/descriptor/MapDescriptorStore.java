package dzida.server.app.map.descriptor;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;

public class MapDescriptorStore {

    public Scenario getScenario(String name, Integer difficultyLevel) {
        if ("eden".equals(name)) {
            return new OpenWorld(new Key<>("eden"));
        }
        if ("small-island".equals(name)) {
            return new Survival(new Key<>("small-island"), difficultyLevel, ImmutableList.of(
                    new Survival.Spawn(new Point(19, 10)),
                    new Survival.Spawn(new Point(19, 14))
            ));
        }
        throw new RuntimeException("scenario <[" + name + "]>is not valid");

    }
}
