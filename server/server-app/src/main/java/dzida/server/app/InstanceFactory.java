package dzida.server.app;

import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.core.player.PlayerService;
import dzida.server.core.skill.SkillStore;
import dzida.server.core.world.map.WorldMapStore;
import io.netty.channel.EventLoop;

import java.util.Optional;

public class InstanceFactory {
    private final MapDescriptorStore mapDescriptorStore = new MapDescriptorStore();
    private final PlayerService playerService;
    private final Arbiter arbiter;
    private final SkillStore skillStore;
    private final WorldMapStore worldMapStore;

    public InstanceFactory(PlayerService playerService, Arbiter arbiter, SkillStore skillStore, WorldMapStore worldMapStore) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.skillStore = skillStore;
        this.worldMapStore = worldMapStore;
    }

    public Optional<Instance> createInstance(String instanceKey, String mapName, EventLoop eventLoop, Integer difficultyLevel) {
        return mapDescriptorStore.getDescriptor(mapName, difficultyLevel)
                .map(mapDescriptor -> new Instance(instanceKey, mapDescriptor, eventLoop, playerService, arbiter, skillStore, worldMapStore));
    }
}
