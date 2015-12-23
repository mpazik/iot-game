package dzida.server.app.map.descriptor;

import com.google.common.collect.ImmutableList;
import dzida.server.core.position.model.Position;

import java.util.Optional;

public class MapDescriptorStore {


    public Optional<Scenario> getDescriptor(String name, Integer difficultyLevel) {
        if ("eden".equals(name)) {
            return Optional.of(new OpenWorld("eden"));
        }
        if ("small-island".equals(name)){
            return Optional.of(new Survival("small-island", difficultyLevel, ImmutableList.of(
                    new Survival.Spawn(new Position(19, 10)),
                    new Survival.Spawn(new Position(19, 14))
            )));
        }
        return Optional.empty();
    }


}
