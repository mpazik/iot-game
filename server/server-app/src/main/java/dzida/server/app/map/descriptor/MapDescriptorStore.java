package dzida.server.app.map.descriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dzida.server.app.npc.Npc;
import dzida.server.core.position.model.Position;

import java.util.Map;
import java.util.Optional;

public class MapDescriptorStore {

    Map<String, MapDescriptor> descriptors = ImmutableMap.of(
            "eden", new Scenario("eden", ImmutableList.of()),
            "small-island", new Scenario("small-island", ImmutableList.of(
                    new Scenario.Spawn(new Position(19, 10), Npc.Fighter),
                    new Scenario.Spawn(new Position(19, 14), Npc.Archer)
            ))
    );

    public Optional<MapDescriptor> getDescriptor(String name) {
        return Optional.ofNullable(descriptors.get(name));
    }
}
