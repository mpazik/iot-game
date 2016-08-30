package dzida.server.app.instance;

import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.npc.AiService;
import dzida.server.app.instance.npc.NpcBehaviour;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.SkillLoader;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDb;
import dzida.server.app.store.memory.PositionStoreInMemory;
import dzida.server.app.store.memory.SkillStoreInMemory;
import dzida.server.app.timesync.TimeServiceImpl;
import dzida.server.app.user.User;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.PositionStore;
import dzida.server.core.profiling.Profilings;
import dzida.server.core.scenario.SurvivalScenarioFactory;
import dzida.server.core.scenario.SurvivalScenarioFactory.SurvivalScenario;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillCommandHandler;
import dzida.server.core.skill.SkillService;
import dzida.server.core.skill.SkillStore;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.map.WorldMap;
import dzida.server.core.world.map.WorldMapService;
import dzida.server.core.world.object.WorldObjectService;
import dzida.server.core.world.pathfinding.CollisionBitMap;
import dzida.server.core.world.pathfinding.PathFinder;
import dzida.server.core.world.pathfinding.PathFinderFactory;

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

        Map<Id<Skill>, Skill> skills = new SkillLoader(staticDataLoader).loadSkills();
        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader));
        SkillStore skillStore = new SkillStoreInMemory(skills);
        WorldObjectStoreMapDb worldObjectStore = new WorldObjectStoreMapDb(instanceKey);

        WorldMap worldMap = worldMapStore.getMap(worldMapKey);
        PositionStore positionStore = new PositionStoreInMemory(worldMap.getSpawnPoint());
        WorldObjectService worldObjectService = WorldObjectService.create(worldObjectStore);

        TimeService timeService = new TimeServiceImpl();
        CharacterService characterService = CharacterService.create();
        WorldMapService worldMapService = WorldMapService.create(worldMapStore, worldMapKey);
        SkillService skillService = SkillService.create(skillStore, timeService);
        PositionService positionService = PositionService.create(positionStore, timeService);

        CollisionBitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));
        PathFinder pathFinder = Profilings.printTime("Collision map built", () -> new PathFinderFactory().createPathFinder(collisionBitMap));
        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService, pathFinder);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService, worldObjectService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService, skillService, characterService);

        instanceStateManager = new InstanceStateManager(positionService, characterService, worldMapService, skillService, worldObjectService);
        commandResolver = new CommandResolver(positionCommandHandler, skillCommandHandler, characterCommandHandler);

        Optional<SurvivalScenario> survivalScenario = createSurvivalScenario(scenario);
        NpcBehaviour npcBehaviour = new NpcBehaviour(timeService, instanceStateManager);
        AiService aiService = new AiService(npcBehaviour);

        this.gameLogic = new GameLogic(scheduler, instanceStateManager, survivalScenario, scenario, aiService, this::handleCommand);
    }

    public void start() {
        instanceStateManager.getEventPublisherBeforeChanges().subscribe(gameLogic::processEventBeforeChanges);
        gameLogic.start();
    }

    public Map<String, Object> getState() {
        return instanceStateManager.getState();
    }

    public void subscribeChange(Consumer<GameEvent> subscriber) {
        instanceStateManager.getEventPublisher().subscribe(subscriber);
    }

    private Optional<SurvivalScenario> createSurvivalScenario(Scenario scenario) {
        SurvivalScenarioFactory survivalScenarioFactory = new SurvivalScenarioFactory();
        if (scenario instanceof Survival) {
            Survival survival = (Survival) scenario;
            return Optional.of(survivalScenarioFactory.createSurvivalScenario(survival.getDifficultyLevel()));
        }
        return Optional.empty();
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

    public static final class UserCharacterMessage implements GameEvent {
        public final Id<Character> characterId;
        public final Id<User> userId;
        public final String userNick;

        public UserCharacterMessage(Id<Character> characterId, Id<User> userId, String userNick) {
            this.characterId = characterId;
            this.userId = userId;
            this.userNick = userNick;
        }
    }
}

