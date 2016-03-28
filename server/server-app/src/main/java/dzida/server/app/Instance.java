package dzida.server.app;

import com.google.common.collect.ImmutableList;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
import dzida.server.app.npc.AiService;
import dzida.server.app.npc.NpcBehaviour;
import dzida.server.app.store.memory.PositionStoreInMemory;
import dzida.server.core.Scheduler;
import dzida.server.core.abilities.Abilities;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.command.SpawnCharacterCommand;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.chat.ChatService;
import dzida.server.core.entity.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Instance {

    private final CommandResolver commandResolver;
    private final GameEventDispatcher gameEventDispatcher;
    private final CommandHandler commandHandler;
    private final PlayerService playerService;
    private final CharacterService characterService;
    private final String instanceKey;

    private final Map<ChannelId, Player.Id> playerChannels = new HashMap<>();
    private final Map<ChannelId, Packet.Builder> packetBuilders = new HashMap<>();
    private final Map<Player.Id, CharacterId> characterIds = new HashMap<>();

    private final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventLoop());
    private final GameLogic gameLogic;
    private final Arbiter arbiter;
    private final boolean isOnlyScenario;
    private final Serializer serializer;
    private final ChangesStore changesStore;

    public Instance(
            String instanceKey,
            Scenario scenario,
            EventLoop eventLoop,
            Serializer serializer,
            WorldMapStore worldMapStore,
            WorldObjectStore worldObjectStore,
            Arbiter arbiter,
            PlayerService playerService,
            TimeService timeService,
            CharacterService characterService,
            SkillService skillService,
            ImmutableList<EntityDescriptor> entityDescriptors, ChangesStore changesStore) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.instanceKey = instanceKey;
        this.serializer = serializer;
        this.characterService = characterService;
        this.changesStore = changesStore;

        WorldMap worldMap = worldMapStore.getMap(scenario.getWorldMapKey());
        PositionStore positionStore = new PositionStoreInMemory(worldMap.getSpawnPoint());
        ChatService chatService = new ChatService(playerService);
        WorldObjectService worldObjectService = WorldObjectService.create(worldObjectStore);

        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(timeService);

        WorldMapService worldMapService = WorldMapService.create(worldMapStore, scenario.getWorldMapKey());

        PositionService positionService = PositionService.create(positionStore, timeService);

        Optional<SurvivalScenario> survivalScenario = createSurvivalScenario(scenario);
        isOnlyScenario = survivalScenario.isPresent();

        gameEventDispatcher = new GameEventDispatcher(positionService, characterService, worldMapService, skillService, worldObjectService, scenario, timeService);

        Scheduler scheduler = new SchedulerImpl(eventLoop);

        CollisionBitMap collisionBitMap = CollisionBitMap.createForWorldMap(worldMap, worldMapStore.getTileset(worldMap.getTileset()));
        PathFinder pathFinder = Profilings.printTime("Collision map built", () -> new PathFinderFactory().createPathFinder(collisionBitMap));
        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService, pathFinder);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService, worldObjectService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService, skillService);

        commandResolver = new CommandResolver(this.serializer, positionCommandHandler, skillCommandHandler, characterCommandHandler, timeSynchroniser, arbiter, playerService, chatService);

        NpcBehaviour npcBehaviour = new NpcBehaviour(positionService, characterService, skillService, timeService, skillCommandHandler, positionCommandHandler);
        AiService aiService = new AiService(npcBehaviour);

        gameLogic = new GameLogic(scheduler, gameEventDispatcher, characterService, playerService, survivalScenario, scenario, this::send, aiService, positionStore, commandResolver, characterCommandHandler);

        Consumer<EntityChangesWithType<?>> changesToSend = changes ->
                packetBuilders.values().stream().forEach(builder -> builder.addChanges(changes));

        Consumer<EntityChangesWithType<?>> changesStoreUpdater = changes -> {
            //noinspection unchecked
            changes.changes.stream()
                    .map(change -> new ChangeDto(change, changes.entityId, changes.entityType))
                    .forEach(changesStore::save);
        };

        commandHandler = new CommandHandler(entityDescriptors, ImmutableList.of(
                changesStoreUpdater,
                changesToSend
        ));
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

        Packet.Builder packetBuilder = Packet.builder();
        packetBuilders.put(channelId, packetBuilder);

        Player.Entity playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);
        gameLogic.playerJoined(character);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, addDataToSend(channel));
        gameEventDispatcher.sendInitialPacket(characterId, playerId, playerEntity);

        commandHandler.handle(new SpawnCharacterCommand(characterId.id()));
        Stream<EntityChangesWithType> instanceEntitiesChanges = getInstanceEntitiesChanges();
        instanceEntitiesChanges.forEach(packetBuilder::addChanges);
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
        packetBuilders.remove(channelId);
        gameEventDispatcher.unregisterCharacter(characterId);
        if (characterService.isCharacterLive(characterId)) {
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
        return (data) -> addDataToSend(channel, new LegacyWsMessage(data.getId(), data));
    }

    private void addDataToSend(Channel channel, LegacyWsMessage data) {
        packetBuilders.get(channel.id()).addLegacyWsMessage(data);
    }

    private void send() {
        for (Channel channel : channels) {
            Packet.Builder builder = this.packetBuilders.get(channel.id());
            if (builder.isEmpty()) {
                continue;
            }
            channel.writeAndFlush(new TextWebSocketFrame(serializer.toJson(builder.build())));
            packetBuilders.put(channel.id(), Packet.builder());
        }
    }

    public Stream<EntityChangesWithType> getInstanceEntitiesChanges() {
        Stream<EntityId<Abilities>> abilitiesIds = characterIds.values().stream().map(id -> new EntityId<>(id.getValue()));
        return abilitiesIds.map(id -> {
            List<Change<Abilities>> changes = changesStore.getChanges(EntityTypes.abilities, id).collect(Collectors.toList());
            return new EntityChangesWithType<>(id, EntityTypes.abilities, changes);
        });
    }
}

