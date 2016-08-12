package dzida.server.app;

import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDb;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDbFactory;
import dzida.server.core.Scheduler;
import dzida.server.core.player.PlayerService;
import dzida.server.core.skill.SkillStore;
import dzida.server.core.world.map.WorldMapStore;

import java.util.Optional;

public class InstanceFactory {
    private final MapDescriptorStore mapDescriptorStore = new MapDescriptorStore();
    private final PlayerService playerService;
    private final Gate gate;
    private final SkillStore skillStore;
    private final WorldMapStore worldMapStore;
    private final WorldObjectStoreMapDbFactory worldObjectStoreFactory;
    private final Container container;

    public InstanceFactory(PlayerService playerService, Gate gate, SkillStore skillStore, WorldMapStore worldMapStore, WorldObjectStoreMapDbFactory worldObjectStoreFactory, Container container) {
        this.playerService = playerService;
        this.gate = gate;
        this.skillStore = skillStore;
        this.worldMapStore = worldMapStore;
        this.worldObjectStoreFactory = worldObjectStoreFactory;
        this.container = container;
    }

    public Optional<Instance> createInstance(String instanceKey, String mapName, Scheduler scheduler, Integer difficultyLevel) {
        WorldObjectStoreMapDb worldObjectStore= worldObjectStoreFactory.createForInstnace(instanceKey);
        return mapDescriptorStore.getDescriptor(mapName, difficultyLevel)
                .map(mapDescriptor -> new Instance(instanceKey, mapDescriptor, scheduler, playerService, gate, skillStore, worldMapStore, worldObjectStore, container));
    }
}
