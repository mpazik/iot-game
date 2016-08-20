package dzida.server.app.instance;

import dzida.server.app.TimeServiceImpl;
import dzida.server.app.command.CharacterCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.KillCharacterCommand;
import dzida.server.app.instance.command.SpawnCharacterCommand;
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
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Instance {

    private final CommandResolver commandResolver;
    private final InstanceStateManager instanceStateManager;

    private final PlayerService playerService;
    private final String instanceKey;
    private final StateSynchroniser stateSynchroniser;

    private final Map<Id<Player>, Id<Character>> characterIds = new HashMap<>();

    private final GameLogic gameLogic;

    public Instance(String instanceKey, Scenario scenario, Scheduler scheduler, PlayerService playerService) {
        this.playerService = playerService;
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

        stateSynchroniser = new StateSynchroniser(instanceStateManager, scenario);
        instanceStateManager.getEventPublisher().subscribe(stateSynchroniser::syncStateChange);
        Optional<SurvivalScenario> survivalScenario = createSurvivalScenario(scenario);
        NpcBehaviour npcBehaviour = new NpcBehaviour(timeService, instanceStateManager);
        AiService aiService = new AiService(npcBehaviour);

        this.gameLogic = new GameLogic(scheduler, instanceStateManager, playerService, survivalScenario, scenario, aiService, this::handleCommand);
    }

    public void start() {
        instanceStateManager.getEventPublisherBeforeChanges().subscribe(gameLogic::processEventBeforeChanges);
        gameLogic.start();
    }

    private Optional<SurvivalScenario> createSurvivalScenario(Scenario scenario) {
        SurvivalScenarioFactory survivalScenarioFactory = new SurvivalScenarioFactory();
        if (scenario instanceof Survival) {
            Survival survival = (Survival) scenario;
            return Optional.of(survivalScenarioFactory.createSurvivalScenario(survival.getDifficultyLevel()));
        }
        return Optional.empty();
    }

    public void addPlayer(Id<Player> playerId, Consumer<GameEvent> sendToPlayer) {
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));
        characterIds.put(playerId, characterId);
        Player playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);
        gameLogic.playerJoined(character);
        handleCommand(new SpawnCharacterCommand(character));
        stateSynchroniser.registerCharacter(playerId, sendToPlayer);
        stateSynchroniser.sendInitialPacket(characterId, playerId, playerEntity);
        System.out.println(String.format("Instance: %s - character %s joined", instanceKey, characterId));
    }

    public void removePlayer(Id<Player> playerId) {
        Id<Character> characterId = characterIds.get(playerId);
        characterIds.remove(playerId);
        stateSynchroniser.unregisterListener(playerId);
        handleCommand(new KillCharacterCommand(characterId));
        System.out.println(String.format("Instance: %s - character %s quit", instanceKey, characterId));
    }

    public void handleCommand(Id<Player> playerId, CharacterCommand characterCommand) {
        Id<Character> characterId = characterIds.get(playerId);
        InstanceCommand instanceCommand = characterCommand.getInstanceCommand(characterId);
        handleCommand(instanceCommand);
    }

    public void handleCommand(InstanceCommand command) {
        List<GameEvent> gameEvents = commandResolver.handleCommand(command);
        instanceStateManager.updateState(gameEvents);
    }

    public void shutdown() {
    }

    public boolean isEmpty() {
        return stateSynchroniser.areAnyListeners();
    }

    public String getKey() {
        return instanceKey;
    }
}

