package dzida.server.app.instance;

import dzida.server.app.Configuration;
import dzida.server.app.Scheduler;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.Result;
import dzida.server.app.basic.entity.GeneralEntity;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.entity.Key;
import dzida.server.app.basic.unit.BitMap;
import dzida.server.app.basic.unit.Point;
import dzida.server.app.instance.character.CharacterCommandHandler;
import dzida.server.app.instance.character.CharacterService;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.position.PositionCommandHandler;
import dzida.server.app.instance.position.PositionService;
import dzida.server.app.instance.position.PositionStore;
import dzida.server.app.instance.skill.Skill;
import dzida.server.app.instance.skill.SkillCommandHandler;
import dzida.server.app.instance.skill.SkillService;
import dzida.server.app.instance.skill.SkillStore;
import dzida.server.app.instance.world.event.WorldObjectCreated;
import dzida.server.app.instance.world.map.WorldMap;
import dzida.server.app.instance.world.map.WorldMapService;
import dzida.server.app.instance.world.object.WorldObject;
import dzida.server.app.instance.world.object.WorldObjectKind;
import dzida.server.app.instance.world.object.WorldObjectService;
import dzida.server.app.instance.world.pathfinding.CollisionBitMap;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.parcel.ParcelChange;
import dzida.server.app.parcel.ParcelCommandHandler;
import dzida.server.app.parcel.ParcelService;
import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.SkillLoader;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.store.http.loader.WorldObjectKindLoader;
import dzida.server.app.store.memory.PositionStoreInMemory;
import dzida.server.app.store.memory.SkillStoreInMemory;
import dzida.server.app.store.memory.WorldObjectStoreInMemory;
import dzida.server.app.time.TimeService;
import dzida.server.app.timesync.TimeServiceImpl;
import dzida.server.app.user.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Instance {

    private final CommandResolver commandResolver;
    private final InstanceStateManager instanceStateManager;

    private final String instanceKey;

    private final GameLogic gameLogic;

    public Instance(String instanceKey, Scenario scenario, Scheduler scheduler) {
        this.instanceKey = instanceKey;

        Key<WorldMap> worldMapKey = scenario.getWorldMapKey();
        StaticDataLoader staticDataLoader = new StaticDataLoader();
        TimeService timeService = new TimeServiceImpl();

        Map<Id<Skill>, Skill> skills = new SkillLoader(staticDataLoader).loadSkills();
        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader), timeService);
        SkillStore skillStore = new SkillStoreInMemory(skills);

        WorldMap worldMap = worldMapStore.getMap(worldMapKey);
        PositionStore positionStore = new PositionStoreInMemory(worldMap.getSpawnPoint());
        List<WorldObjectKind> worldObjectKinds = new WorldObjectKindLoader(staticDataLoader).loadWorldObjectKinds();
        WorldObjectStoreInMemory worldObjectStore = new WorldObjectStoreInMemory(worldObjectKinds, timeService);
        WorldObjectService worldObjectService = WorldObjectService.create(worldObjectStore);
        BitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));

        CharacterService characterService = CharacterService.create();
        WorldMapService worldMapService = WorldMapService.create(worldMapStore, worldMapKey);
        SkillService skillService = SkillService.create(skillStore, timeService);
        PositionService positionService = PositionService.create(positionStore, timeService, worldObjectStore, collisionBitMap);

        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService, worldObjectService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService, skillService, characterService);
        ParcelCommandHandler parcelCommandHandler = new ParcelCommandHandler();
        ParcelService parcelService = new ParcelService();

        instanceStateManager = new InstanceStateManager(positionService, characterService, worldMapService, skillService, worldObjectService, parcelService);
        commandResolver = new CommandResolver(positionCommandHandler, skillCommandHandler, characterCommandHandler, parcelCommandHandler);

        this.gameLogic = new GameLogic(scheduler, instanceStateManager);

        createSpawningParcel(worldMap, parcelService);
        initGameObjects(worldMapKey, worldMapStore, worldObjectStore, worldObjectService);
    }

    private void createSpawningParcel(WorldMap worldMap, ParcelService parcelService) {
        Point spawnPoint = worldMap.getSpawnPoint();
        int spawningParcelX = (int) (spawnPoint.getX()) / Configuration.ParcelSize;
        int spawningParcelY = (int) (spawnPoint.getY()) / Configuration.ParcelSize;
        parcelService.processEvent(new ParcelChange.ParcelClaimed(spawningParcelX, spawningParcelY, new Id<>(0), "Kingdom", "Common land"));
    }

    private void initGameObjects(Key<WorldMap> worldMapKey, WorldMapStoreHttp worldMapStore, WorldObjectStoreInMemory worldObjectStore, WorldObjectService worldObjectService) {
        if (worldObjectService.getState().isEmpty()) {
            List<WorldObject> initialMapObjects = worldMapStore.getInitialMapObjects(worldMapKey);
            initialMapObjects.forEach(worldObject -> {
                WorldObjectKind worldObjectKind = worldObjectStore.getWorldObjectKind(worldObject.getKind());
                int y = worldObject.getY() - worldObjectKind.getHeight() + 1;
                Optional<GeneralEntity<WorldObject>> objectEntity = worldObjectService.createWorldObject(worldObject.getKind(), worldObject.getX(), y);
                objectEntity.map(WorldObjectCreated::new).ifPresent(instanceStateManager::dispatchEvent);
            });
        }
    }

    public void start() {
        gameLogic.start();
    }

    public Map<String, Object> getState() {
        return instanceStateManager.getState();
    }

    public void subscribeChange(Consumer<GameEvent> subscriber) {
        instanceStateManager.getEventPublisher().subscribe(subscriber);
    }

    public Result handleCommand(InstanceCommand command) {
        // temporary validation done here. Eventually this will be asynchronous so it couldn't return any response.
        // in that case validation would have to be performed on InstanceServer on a copy of the state that may be a bit outdated.
        Outcome<List<GameEvent>> gameEvents = commandResolver.handleCommand(command);
        gameEvents.toOptional().ifPresent(instanceStateManager::updateState);
        return gameEvents.toResult();
    }

    public String getKey() {
        return instanceKey;
    }

    public static final class UserCharacter implements GameEvent {
        public final Id<Character> characterId;
        public final Id<User> userId;
        public final String userNick;

        public UserCharacter(Id<Character> characterId, Id<User> userId, String userNick) {
            this.characterId = characterId;
            this.userId = userId;
            this.userNick = userNick;
        }
    }
}

