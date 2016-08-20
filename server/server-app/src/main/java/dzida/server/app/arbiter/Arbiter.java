package dzida.server.app.arbiter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import dzida.server.app.BasicJsonSerializer;
import dzida.server.app.Configuration;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceServer;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class Arbiter implements VerifyingConnectionServer<String, String> {
    private final ServerDispatcher serverDispatcher;
    private final PlayerService playerService;
    private final Scheduler scheduler;
    private final JsonProtocol arbiterProtocol = new JsonProtocol.Builder()
            .registerParsingMessageType(1, JoinBattleCommand.class)
            .registerParsingMessageType(2, GoHomeCommand.class)
            .registerSerializationMessageType(1, JoinToInstance.class)
            .registerTypeHierarchyAdapter(Id.class, BasicJsonSerializer.idTypeAdapter)
            .registerTypeHierarchyAdapter(Key.class, BasicJsonSerializer.keyTypeAdapter)
            .build();

    private final Map<Id<Player>, Key<Instance>> playersInstances;
    private final Set<Key<Instance>> initialInstances;
    private final Set<Id<Player>> playingPlayers;
    private final Key<Instance> defaultInstance;

    public Arbiter(ServerDispatcher serverDispatcher, PlayerService playerService, Scheduler scheduler) {
        this.serverDispatcher = serverDispatcher;
        this.playerService = playerService;
        this.scheduler = scheduler;
        playersInstances = new HashMap<>();
        initialInstances = ImmutableList.copyOf(Configuration.getInitialInstances())
                .stream()
                .map((Function<String, Key<Instance>>) Key::new)
                .collect(Collectors.toSet());
        playingPlayers = new HashSet<>();
        defaultInstance = new Key<>(Configuration.getInitialInstances()[0]);
    }

    public void start() {
        initialInstances.forEach(instanceKey -> {
            startInstance(instanceKey.getValue(), null);
        });
    }

    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);

        String instanceKeyValue = instanceKey.getValue();
        InstanceServer instanceServer = new InstanceServer(playerService, scheduler, this, instanceKey, instanceType, difficultyLevel);

        serverDispatcher.addServer(instanceKeyValue, instanceServer);

        return instanceKey;
    }

    private Key<Instance> generateInstanceKey(String instanceType, Integer difficultyLevel) {
        if (difficultyLevel == null) {
            return new Key<>(instanceType);
        }
        return new Key<>(instanceType + '_' + difficultyLevel + '_' + new Random().nextInt(10000000));
    }

    public boolean isPlayerPlaying(String nick) {
        Optional<Id<Player>> playerIdOpt = playerService.findPlayer(nick);
        return playerIdOpt.isPresent() && playingPlayers.contains(playerIdOpt.get());
    }

    private Optional<Id<Player>> findOrCreatePlayer(String nick) {
        Optional<Id<Player>> playerIdOpt = playerService.findPlayer(nick);
        if (playerIdOpt.isPresent()) {
            return playerIdOpt;
        }
        Outcome<Player> player = playerService.createPlayer(nick);
        return player.toOptional().map(Player::getId);
    }

    @Override
    public Result verifyConnection(String connectionData) {
        return Result.ok();
    }

    @Override
    public void onConnection(Connector<String> connector, String connectionData) {
        if (Strings.isNullOrEmpty(connectionData)) {
            connector.onClose();
            return;
        }

        Optional<Id<Player>> playerIdOpt = findOrCreatePlayer(connectionData);
        if (!playerIdOpt.isPresent()) {
            connector.onClose();
            return;
        }
        Id<Player> playerId = playerIdOpt.get();
        playingPlayers.add(playerId);
        playersInstances.put(playerId, defaultInstance);
        connector.onMessage(arbiterProtocol.serializeMessage(new JoinToInstance(defaultInstance)));

        connector.onOpen(new ServerConnection<String>() {
            @Override
            public void send(String data) {
                Object message = arbiterProtocol.parseMessage(data);
                whenTypeOf(message)
                        .is(JoinBattleCommand.class)
                        .then(command -> {
                            Player.Data playerData = playerService.getPlayer(playerId).getData();
                            Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), command.difficultyLevel);
                            playerService.updatePlayerData(playerId, updatedPlayerData);
                            Key<Instance> newInstanceKey = startInstance(command.map, command.difficultyLevel);
                            playersInstances.put(playerId, newInstanceKey);
                            connector.onMessage(arbiterProtocol.serializeMessage(new JoinToInstance(newInstanceKey)));
                        })
                        .is(GoHomeCommand.class)
                        .then(command -> {
                            playersInstances.put(playerId, defaultInstance);
                            connector.onMessage(arbiterProtocol.serializeMessage(new JoinToInstance(defaultInstance)));
                        });
            }


            @Override
            public void close() {
                playingPlayers.remove(playerId);
            }
        });
    }

    public Optional<Id<Player>> authenticate(Key<Instance> instanceKey, String connectionData) {
        return findOrCreatePlayer(connectionData).filter(playerId -> playersInstances.get(playerId).equals(instanceKey));
    }

    private static class JoinToInstance {
        final Key<Instance> instanceKey;

        JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    public class JoinBattleCommand {
        public final String map;
        public final int difficultyLevel;

        public JoinBattleCommand(String map, int difficultyLevel) {
            this.map = map;
            this.difficultyLevel = difficultyLevel;
        }
    }

    public class GoHomeCommand {
    }
}
