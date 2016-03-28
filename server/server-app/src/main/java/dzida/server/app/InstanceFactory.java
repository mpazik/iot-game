package dzida.server.app;

import com.google.common.collect.ImmutableList;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDb;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDbFactory;
import dzida.server.core.character.CharacterService;
import dzida.server.core.entity.ChangesStore;
import dzida.server.core.entity.EntityDescriptor;
import dzida.server.core.player.PlayerService;
import dzida.server.core.skill.SkillService;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.map.WorldMapStore;
import io.netty.channel.EventLoop;

import java.util.Optional;

public class InstanceFactory {
    private final MapDescriptorStore mapDescriptorStore = new MapDescriptorStore();
    private final PlayerService playerService;
    private final Arbiter arbiter;
    private final WorldMapStore worldMapStore;
    private final WorldObjectStoreMapDbFactory worldObjectStoreFactory;
    private final Serializer serializer;
    private final TimeService timeService;
    private final CharacterService characterService;
    private final SkillService skillService;
    private final ImmutableList<EntityDescriptor> entityDescriptors;
    private ChangesStore changesStore;

    public InstanceFactory(
            PlayerService playerService,
            Arbiter arbiter,
            WorldMapStore worldMapStore,
            WorldObjectStoreMapDbFactory worldObjectStoreFactory,
            Serializer serializer,
            TimeService timeService,
            CharacterService characterService,
            SkillService skillService,
            ImmutableList<EntityDescriptor> entityDescriptors,
            ChangesStore changesStore) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.worldMapStore = worldMapStore;
        this.worldObjectStoreFactory = worldObjectStoreFactory;
        this.serializer = serializer;
        this.timeService = timeService;
        this.characterService = characterService;
        this.skillService = skillService;
        this.entityDescriptors = entityDescriptors;
        this.changesStore = changesStore;
    }

    public Optional<Instance> createInstance(String instanceKey, String mapName, EventLoop eventLoop, Integer difficultyLevel) {
        WorldObjectStoreMapDb worldObjectStore = worldObjectStoreFactory.createForInstnace(instanceKey);
        return mapDescriptorStore.getDescriptor(mapName, difficultyLevel)
                .map(mapDescriptor -> new Instance(instanceKey, mapDescriptor, eventLoop, serializer, worldMapStore, worldObjectStore, arbiter, playerService, timeService, characterService, skillService, entityDescriptors, changesStore));
    }
}
