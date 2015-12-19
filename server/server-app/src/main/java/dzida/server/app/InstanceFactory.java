package dzida.server.app;

import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.core.player.PlayerService;
import io.netty.channel.EventLoop;

import java.util.Optional;

public class InstanceFactory {
    private final MapDescriptorStore mapDescriptorStore = new MapDescriptorStore();
    private final PlayerService playerService;
    private final Arbiter arbiter;

    public InstanceFactory(PlayerService playerService, Arbiter arbiter) {
        this.playerService = playerService;
        this.arbiter = arbiter;
    }

    public Optional<Instance> createInstance(String mapName, EventLoop eventLoop, Integer difficultyLevel) {
        return mapDescriptorStore.getDescriptor(mapName, difficultyLevel).map(mapDescriptor -> new Instance(mapDescriptor, eventLoop, playerService, arbiter));
    }
}
