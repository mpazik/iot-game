package dzida.server.app.arbiter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import dzida.server.app.Configuration;
import dzida.server.app.chat.Chat;
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
    private final Chat chat;
    private final PlayerService playerService;
    private final Scheduler scheduler;
    private final JsonProtocol arbiterProtocol;

    private final Map<Id<Player>, Key<Instance>> playersInstances;
    private final Map<Key<Instance>, InstanceServer> instances;
    private final Set<Key<Instance>> initialInstances;
    private final Set<Id<Player>> playingPlayers;
    private final Key<Instance> defaultInstance;
    private final Set<Key<Instance>> instancesToShutdown;

    public Arbiter(ServerDispatcher serverDispatcher, Chat chat, PlayerService playerService, Scheduler scheduler) {
        this.serverDispatcher = serverDispatcher;
        this.chat = chat;
        this.playerService = playerService;
        this.scheduler = scheduler;
        playersInstances = new HashMap<>();
        instances = new HashMap<>();
        initialInstances = ImmutableList.copyOf(Configuration.getInitialInstances())
                .stream()
                .map((Function<String, Key<Instance>>) Key::new)
                .collect(Collectors.toSet());
        playingPlayers = new HashSet<>();
        instancesToShutdown = new HashSet<>();
        defaultInstance = new Key<>(Configuration.getInitialInstances()[0]);
        arbiterProtocol = ArbiterProtocol.createSerializer();
    }

    public void start() {
        System.out.println("Arbiter: started system");
        initialInstances.forEach(instanceKey -> {
            startInstance(instanceKey.getValue(), null);
        });
    }

    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);

        String instanceKeyValue = instanceKey.getValue();
        InstanceServer instanceServer = new InstanceServer(playerService, scheduler, this, instanceKey, instanceType, difficultyLevel);

        serverDispatcher.addServer(instanceKeyValue, instanceServer);
        instances.put(instanceKey, instanceServer);
        chat.createInstanceChannel(instanceKey);
        System.out.println("Arbiter: started instance: " + instanceKey);
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
        playerService.loginPlayer(playerId);

        ArbiterConnection arbiterConnection = new ArbiterConnection(playerId, connector);
        connector.onOpen(arbiterConnection);
        arbiterConnection.movePlayerToInstance(defaultInstance);
    }

    public Optional<Id<Player>> authenticate(Key<Instance> instanceKey, String connectionData) {
        return findOrCreatePlayer(connectionData).filter(playerId -> playersInstances.get(playerId).equals(instanceKey));
    }

    public void endOfScenario(Key<Instance> instanceKey) {
        InstanceServer instanceServer = instances.get(instanceKey);
        if (instanceServer.isEmpty()) {
            shutdownInstanced(instanceKey);
        } else {
            instancesToShutdown.add(instanceKey);
        }
    }

    public void shutdownInstanced(Key<Instance> instanceKey) {
        serverDispatcher.removeServer(instanceKey.getValue());
        instances.remove(instanceKey);
        chat.closeInstanceChannel(instanceKey);
        System.out.println("Arbiter: shutdown instance: " + instanceKey);
    }

    private final class ArbiterConnection implements ServerConnection<String> {
        private final Id<Player> playerId;
        private final Connector<String> connector;

        ArbiterConnection(Id<Player> playerId, Connector<String> connector) {
            this.playerId = playerId;
            this.connector = connector;
        }

        @Override
        public void send(String data) {
            Object message = arbiterProtocol.parseMessage(data);
            whenTypeOf(message)
                    .is(JoinBattleCommand.class)
                    .then(command -> {
                        removePlayerFromLastInstance();
                        Player.Data playerData = playerService.getPlayer(playerId).getData();
                        Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), command.difficultyLevel);
                        playerService.updatePlayerData(playerId, updatedPlayerData);
                        Key<Instance> newInstanceKey = startInstance(command.map, command.difficultyLevel);
                        movePlayerToInstance(newInstanceKey);
                    })
                    .is(GoHomeCommand.class)
                    .then(command -> {
                        removePlayerFromLastInstance();
                        movePlayerToInstance(defaultInstance);
                    });
        }

        public void movePlayerToInstance(Key<Instance> newInstanceKey) {
            playersInstances.put(playerId, newInstanceKey);
            connector.onMessage(arbiterProtocol.serializeMessage(new JoinToInstance(newInstanceKey)));
            System.out.println("Arbiter: player: " + playerId + " assigned to instance: " + newInstanceKey);
        }

        private void removePlayerFromLastInstance() {
            Key<Instance> lastInstanceKey = playersInstances.get(playerId);
            if (lastInstanceKey == null) return;
            InstanceServer instanceServer = instances.get(lastInstanceKey);
            instanceServer.disconnectPlayer(playerId);
            System.out.println("Arbiter: player: " + playerId + " removed from instance: " + lastInstanceKey);
            tryToKillInstance(lastInstanceKey);
        }

        private void tryToKillInstance(Key<Instance> instanceKey) {
            if (instancesToShutdown.contains(instanceKey) && instances.get(instanceKey).isEmpty()) {
                shutdownInstanced(instanceKey);
            }
        }

        @Override
        public void close() {
            playerService.logoutPlayer(playerId);
            playingPlayers.remove(playerId);
        }
    }
}
