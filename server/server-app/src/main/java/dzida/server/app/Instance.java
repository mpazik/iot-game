package dzida.server.app;

import com.google.common.collect.Lists;
import dzida.server.app.command.InstanceCommand;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.npc.NpcBehaviour;
import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.SkillLoader;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDb;
import dzida.server.app.store.memory.PositionStoreInMemory;
import dzida.server.app.store.memory.SkillStoreInMemory;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.chat.ChatService;
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

import static dzida.server.app.Serializer.getSerializer;

public class Instance {

    private final CommandResolver commandResolver;
    private final InstanceStateManager instanceStateManager;
    private final PlayerService playerService;
    private final CharacterService characterService;
    private final String instanceKey;

    private final Map<Id<Player>, Consumer<String>> playerSends = new HashMap<>();
    private final Map<Id<Player>, List<Object>> messagesToSend = new HashMap<>();
    private final Map<Id<Player>, Id<Character>> characterIds = new HashMap<>();

    private final GameLogic gameLogic;

    public Instance(String instanceKey, Scenario scenario, Scheduler scheduler, PlayerService playerService) {
        this.playerService = playerService;
        this.instanceKey = instanceKey;

        StaticDataLoader staticDataLoader = new StaticDataLoader();

        Map<Id<Skill>, Skill> skills = new SkillLoader(staticDataLoader).loadSkills();
        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader));
        SkillStore skillStore = new SkillStoreInMemory(skills);
        WorldObjectStoreMapDb worldObjectStore = new WorldObjectStoreMapDb(instanceKey);

        WorldMap worldMap = worldMapStore.getMap(scenario.getWorldMapKey());
        PositionStore positionStore = new PositionStoreInMemory(worldMap.getSpawnPoint());
        ChatService chatService = new ChatService(playerService);
        WorldObjectService worldObjectService = WorldObjectService.create(worldObjectStore);

        TimeService timeService = new TimeServiceImpl();
        characterService = CharacterService.create();
        WorldMapService worldMapService = WorldMapService.create(worldMapStore, scenario.getWorldMapKey());
        SkillService skillService = SkillService.create(skillStore, timeService);
        PositionService positionService = PositionService.create(positionStore, timeService);

        Optional<SurvivalScenario> survivalScenario = createSurvivalScenario(scenario);

        instanceStateManager = new InstanceStateManager(positionService, characterService, worldMapService, skillService, worldObjectService, scenario, timeService);

        CollisionBitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));
        PathFinder pathFinder = Profilings.printTime("Collision map built", () -> new PathFinderFactory().createPathFinder(collisionBitMap));
        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService, pathFinder);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService, worldObjectService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService, skillService);

        commandResolver = new CommandResolver(positionCommandHandler, skillCommandHandler, characterCommandHandler, chatService);

        NpcBehaviour npcBehaviour = new NpcBehaviour(positionService, characterService, skillService, timeService, skillCommandHandler, positionCommandHandler);
        AiService aiService = new AiService(npcBehaviour);

        this.gameLogic =  new GameLogic(scheduler, instanceStateManager, characterService, playerService, survivalScenario, scenario, this::send, aiService, positionStore, commandResolver, characterCommandHandler);
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

    public void addPlayer(Id<Player> playerId, Consumer<String> sendToPlayer) {
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));
        characterIds.put(playerId, characterId);
        messagesToSend.put(playerId, Lists.newArrayList());
        playerSends.put(playerId, sendToPlayer);
        Player playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);
        gameLogic.playerJoined(character);
        instanceStateManager.dispatchEvents(commandResolver.createCharacter(character));
        instanceStateManager.registerCharacter(character, addDataToSend(playerId));
        instanceStateManager.sendInitialPacket(characterId, playerId, playerEntity);
        System.out.println(String.format("Instance: %s - character %s joined", instanceKey, characterId));
        send();
    }

    public void removePlayer(Id<Player> playerId) {
        Id<Character> characterId = characterIds.get(playerId);
        playerSends.remove(playerId);
        characterIds.remove(playerId);
        messagesToSend.remove(playerId);
        instanceStateManager.unregisterCharacter(characterId);
        if (characterService.isCharacterLive(characterId)){
            instanceStateManager.dispatchEvents(commandResolver.removeCharacter(characterId));
        }
        System.out.println(String.format("Instance: %s - character %s quit", instanceKey, characterId));
        send();
    }

    public void handleCommand(Id<Player> playerId, InstanceCommand command) {
        Id<Character> characterId = characterIds.get(playerId);
        List<GameEvent> gameEvents = commandResolver.handleCommand(command, playerId, characterId);
        instanceStateManager.dispatchEvents(gameEvents);
        send();
    }

    private Consumer<GameEvent> addDataToSend(Id<Player> playerId) {
        return (data) -> addDataToSend(playerId, new Packet(data.getId(), data));
    }

    private void addDataToSend(Id<Player> playerId, Object data) {
        messagesToSend.get(playerId).add(data);
    }

    private void send() {
        for (Id<Player> playerId: messagesToSend.keySet()) {
            List<Object> messagesToSend = this.messagesToSend.get(playerId);
            if (messagesToSend.isEmpty()) {
                continue;
            }
            playerSends.get(playerId).accept(getSerializer().toJson(messagesToSend));
            messagesToSend.clear();
        }
    }

    public void shutdown() {
    }

    public boolean isEmpty() {
        return playerSends.isEmpty();
    }
}

