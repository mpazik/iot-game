package dzida.server.app;

import com.google.common.collect.Lists;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.npc.NpcBehaviour;
import dzida.server.app.store.memory.PositionStoreInMemory;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
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
import dzida.server.core.skill.SkillCommandHandler;
import dzida.server.core.skill.SkillService;
import dzida.server.core.skill.SkillStore;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.map.WorldMap;
import dzida.server.core.world.map.WorldMapService;
import dzida.server.core.world.map.WorldMapStore;
import dzida.server.core.world.object.WorldObjectService;
import dzida.server.core.world.object.WorldObjectStore;
import dzida.server.core.world.pathfinding.CollisionBitMap;
import dzida.server.core.world.pathfinding.PathFinder;
import dzida.server.core.world.pathfinding.PathFinderFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static dzida.server.app.Serializer.getSerializer;

class Instance {

    private final CommandResolver commandResolver;
    private final GameEventDispatcher gameEventDispatcher;
    private final PlayerService playerService;
    private final CharacterService characterService;
    private final String instanceKey;

    private final Map<ChannelId, Player.Id> playerChannels = new HashMap<>();
    private final Map<ChannelId, List<Object>> messagesToSend = new HashMap<>();
    private final Map<Player.Id, CharacterId> characterIds = new HashMap<>();

    private final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventLoop());
    private final GameLogic gameLogic;
    private final Arbiter arbiter;
    private final boolean isOnlyScenario;

    public Instance(String instanceKey, Scenario scenario, EventLoop eventLoop, PlayerService playerService, Arbiter arbiter, SkillStore skillStore, WorldMapStore worldMapStore, WorldObjectStore worldObjectStore) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.instanceKey = instanceKey;
        WorldMap worldMap = worldMapStore.getMap(scenario.getWorldMapKey());
        PositionStore positionStore = new PositionStoreInMemory(worldMap.getSpawnPoint());
        ChatService chatService = new ChatService(playerService);
        WorldObjectService worldObjectService = WorldObjectService.create(worldObjectStore);

        TimeSynchroniser timeSynchroniser = new TimeSynchroniser();
        TimeService timeService = new TimeService();
        characterService = CharacterService.create();
        WorldMapService worldMapService = WorldMapService.create(worldMapStore, scenario.getWorldMapKey());
        SkillService skillService = SkillService.create(skillStore, timeService);
        PositionService positionService = PositionService.create(positionStore, timeService);

        Optional<SurvivalScenario> survivalScenario = createSurvivalScenario(scenario);
        isOnlyScenario = survivalScenario.isPresent();

        gameEventDispatcher = new GameEventDispatcher(positionService, characterService, worldMapService, skillService, worldObjectService, scenario);

        Scheduler scheduler = new SchedulerImpl(eventLoop);

        CollisionBitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));
        PathFinder pathFinder = Profilings.printTime("Collision map built", () -> new PathFinderFactory().createPathFinder(collisionBitMap));
        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService, pathFinder);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService, worldObjectService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService, skillService);

        commandResolver = new CommandResolver(positionCommandHandler, skillCommandHandler, characterCommandHandler, timeSynchroniser, arbiter, playerService, chatService);

        NpcBehaviour npcBehaviour = new NpcBehaviour(positionService, characterService, skillService, timeService, skillCommandHandler, positionCommandHandler);
        AiService aiService = new AiService(npcBehaviour);

        this.gameLogic =  new GameLogic(scheduler, gameEventDispatcher, characterService, playerService, survivalScenario, scenario, this::send, aiService, positionStore, commandResolver, characterCommandHandler);
    }

    public void start() {
        gameEventDispatcher.getEventPublisherBeforeChanges().subscribe(gameLogic::processEventBeforeChanges);

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

    public void addPlayer(Channel channel, Player.Id playerId) {
        channels.add(channel);
        CharacterId characterId = new CharacterId((int) Math.round((Math.random() * 100000)));
        characterIds.put(playerId, characterId);
        ChannelId channelId = channel.id();
        playerChannels.put(channelId, playerId);
        messagesToSend.put(channelId, Lists.newArrayList());
        Player.Entity playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);
        gameLogic.playerJoined(character);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, addDataToSend(channel));
        gameEventDispatcher.sendInitialPacket(characterId, playerId, playerEntity);
        System.out.println(String.format("Instance: %s - character %s joined", instanceKey, characterId));
        send();
    }

    public void removePlayer(Channel channel) {
        channels.remove(channel);
        ChannelId channelId = channel.id();
        Player.Id playerId = playerChannels.get(channelId);
        CharacterId characterId = characterIds.get(playerId);
        characterIds.remove(playerId);
        playerChannels.remove(channelId);
        messagesToSend.remove(channelId);
        gameEventDispatcher.unregisterCharacter(characterId);
        if (characterService.isCharacterLive(characterId)){
            gameEventDispatcher.dispatchEvents(commandResolver.removeCharacter(characterId));
        }
        System.out.println(String.format("Instance: %s - character %s quit", instanceKey, characterId));
        send();
        if (channels.size() == 0 && isOnlyScenario) {
            arbiter.killInstance(instanceKey);
        }
    }

    public void parseMessage(Channel channel, String request) {
        Player.Id playerId = playerChannels.get(channel.id());
        CharacterId characterId = characterIds.get(playerId);
        List<GameEvent> gameEvents = commandResolver.dispatchPacket(playerId, characterId, request, addDataToSend(channel));
        gameEventDispatcher.dispatchEvents(gameEvents);
        send();
    }

    private Consumer<GameEvent> addDataToSend(Channel channel) {
        return (data) -> addDataToSend(channel, new Packet(data.getId(), data));
    }

    private void addDataToSend(Channel channel, Object data) {
        messagesToSend.get(channel.id()).add(data);
    }

    private void send() {
        for (Channel channel : channels) {
            List<Object> messagesToSend = this.messagesToSend.get(channel.id());
            if (messagesToSend.isEmpty()) {
                continue;
            }
            channel.writeAndFlush(new TextWebSocketFrame(getSerializer().toJson(messagesToSend)));
            messagesToSend.clear();
        }
    }
}

