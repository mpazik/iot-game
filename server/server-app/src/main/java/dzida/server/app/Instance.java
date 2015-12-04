package dzida.server.app;

import com.google.common.collect.Lists;
import dzida.server.app.map.descriptor.MapDescriptor;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.npc.AiService;
import dzida.server.app.npc.NpcBehaviour;
import dzida.server.core.CharacterId;
import dzida.server.core.PlayerId;
import dzida.server.core.PlayerService;
import dzida.server.core.Scheduler;
import dzida.server.core.character.CharacterCommandHandler;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Position;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillCommandHandler;
import dzida.server.core.skill.SkillService;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.WorldService;
import dzida.server.core.world.model.WorldState;
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
import java.util.function.Consumer;

import static dzida.server.app.Serializer.getSerializer;

class Instance {
    private final PositionStoreImpl positionStore;

    private final CommandResolver commandResolver;
    private final GameEventDispatcher gameEventDispatcher;
    private final AiService aiService;
    private final PlayerService playerService;
    private final String instanceKey;

    private final Map<ChannelId, CharacterId> charChannels = new HashMap<>();
    private final Map<ChannelId, List<Object>> messagesToSend = new HashMap<>();

    private final ChannelGroup channels = new DefaultChannelGroup(new DefaultEventLoop());

    public Instance(MapDescriptor mapDescriptor, EventLoop eventLoop, PlayerService playerService, Arbiter arbiter) {
        this.playerService = playerService;
        instanceKey = mapDescriptor.getMapName();
        WorldState worldState = new WorldStateStore().loadMap(mapDescriptor.getMapName());
        Map<Integer, Skill> skills = new SkillStore().loadSkills();
        positionStore = new PositionStoreImpl(worldState.getSpawnPoint());

        TimeSynchroniser timeSynchroniser = new TimeSynchroniser();
        TimeService timeService = new TimeService();
        CharacterService characterService = CharacterService.create();
        WorldService worldService = WorldService.create(worldState);
        SkillService skillService = SkillService.create(skills, timeService);
        PositionService positionService = PositionService.create(positionStore, timeService);

        gameEventDispatcher = new GameEventDispatcher(positionService, characterService, worldService, skillService);

        Scheduler scheduler = new SchedulerImpl(eventLoop, gameEventDispatcher);

        PositionCommandHandler positionCommandHandler = new PositionCommandHandler(characterService, positionService, timeService);
        SkillCommandHandler skillCommandHandler = new SkillCommandHandler(timeService, positionService, characterService, skillService);
        CharacterCommandHandler characterCommandHandler = new CharacterCommandHandler(positionService);

        commandResolver = new CommandResolver(positionCommandHandler, skillCommandHandler, characterCommandHandler, timeSynchroniser, arbiter);

        GameLogic gameLogic = new GameLogic(scheduler, positionService, characterService, playerService);
        gameEventDispatcher.getEventPublisherBeforeChanges().subscribe(gameLogic::processEventBeforeChanges);

        NpcBehaviour npcBehaviour = new NpcBehaviour(positionService, characterService, skillService, timeService, skillCommandHandler, positionCommandHandler);
        aiService = new AiService(npcBehaviour);

        if (mapDescriptor instanceof Scenario) {
            Scenario scenario = (Scenario) mapDescriptor;
            initNpcs(scenario.getSpawns());
        }

        scheduler.schedulePeriodically(this::aiTick, 500, 500);
    }

    private void initNpcs(List<Scenario.Spawn> spawns) {
        spawns.stream().forEach(spawn -> addNpc(spawn.getPosition(), spawn.getBotType()));
    }

    public void addPlayer(Channel channel, PlayerId playerId) {
        channels.add(channel);
        CharacterId characterId = new CharacterId((int) Math.round((Math.random() * 100000)));

        ChannelId channelId = channel.id();
        charChannels.put(channelId, characterId);
        messagesToSend.put(channelId, Lists.newArrayList());
        String nick = playerService.getPlayerNick(playerId);
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, addDataToSend(channel));
        gameEventDispatcher.sendInitialPacket(characterId, playerId);
        System.out.println(String.format("Instance: %s - character %s joined", instanceKey, characterId));
        send();
    }

    public void removePlayer(Channel channel) {
        channels.remove(channel);
        ChannelId channelId = channel.id();
        CharacterId characterId = charChannels.get(channelId);
        charChannels.remove(channelId);
        messagesToSend.remove(channelId);
        gameEventDispatcher.unregisterCharacter(characterId);
        gameEventDispatcher.dispatchEvents(commandResolver.removeCharacter(characterId));
        System.out.println(String.format("Instance: %s - character %s quit", instanceKey, characterId));
        send();
    }

    public void addNpc(Position position, int npcType) {
        CharacterId characterId = new CharacterId((int) Math.round((Math.random() * 100000)));
        NpcCharacter character = new NpcCharacter(characterId, npcType);
        positionStore.setPosition(characterId, position);
        aiService.createNpc(npcType, character);
        gameEventDispatcher.dispatchEvents(commandResolver.createCharacter(character));
        gameEventDispatcher.registerCharacter(character, event -> {
            List<GameEvent> gameEvents = aiService.processPacket(characterId, event);
            gameEventDispatcher.dispatchEvents(gameEvents);
        });
    }

    public void parseMessage(Channel channel, String request) {
        CharacterId characterId = charChannels.get(channel.id());
        List<GameEvent> gameEvents = commandResolver.dispatchPacket(characterId, request, addDataToSend(channel));
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

    public void aiTick() {
        gameEventDispatcher.dispatchEvents(aiService.processTick());
        send();
    }
}

