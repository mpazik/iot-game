package dzida.server.app.map.descriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dzida.server.app.npc.Npc;
import dzida.server.core.position.model.Position;

import java.util.Map;
import java.util.Optional;

public class MapDescriptorStore {

    Map<String, Scenario> descriptors = ImmutableMap.of(
            "eden", new OpenWorld("eden"),
            "small-island", new Survival("small-island", ImmutableList.of(
                    new Survival.Spawn(new Position(19, 10), Npc.Fighter),
                    new Survival.Spawn(new Position(19, 14), Npc.Archer)
            ))
    );

    public Optional<Scenario> getDescriptor(String name) {
        return Optional.ofNullable(descriptors.get(name));
    }
}
