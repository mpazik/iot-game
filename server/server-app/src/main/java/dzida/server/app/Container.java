package dzida.server.app;

import dzida.server.app.command.CharacterCommand;
import dzida.server.app.command.JoinBattleCommand;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceProtocol;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.ClientConnection;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class Container implements VerifyingConnectionServer<String, String> {
    private final Scheduler scheduler;
    private final Map<Key<Instance>, Instance> instances = new HashMap<>();
    private final Map<Id<Player>, ClientConnection<String>> connections = new HashMap<>();
    private final Map<Id<Player>, Key<Instance>> playersInstances = new HashMap<>();
    private final MapDescriptorStore mapDescriptorStore;
    private final PlayerService playerService;
    private final Gate gate;
    private final JsonProtocol serializer;

    Container(PlayerService playerService, Scheduler scheduler, Gate gate) {
        this.scheduler = scheduler;
        this.playerService = playerService;
        this.gate = gate;

        mapDescriptorStore = new MapDescriptorStore();
        serializer = InstanceProtocol.createSerializer();

        gate.subscribePlayerJoinedToInstance(this::playerJoinedToInstance);
    }


    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);
        String instanceKeyValue = instanceKey.getValue();

        Optional<Instance> instance = mapDescriptorStore.getDescriptor(instanceType, difficultyLevel)
                .map(mapDescriptor -> new Instance(instanceKeyValue, mapDescriptor, scheduler, playerService));

        if (!instance.isPresent()) {
            System.err.println("map descriptor is not valid: " + instanceType);
            return instanceKey;
        }
        instance.get().start();
        instances.put(instanceKey, instance.get());
        return instanceKey;
    }

    private Key<Instance> generateInstanceKey(String instanceType, Integer difficultyLevel) {
        if (difficultyLevel == null) {
            return new Key<>(instanceType);
        }
        return new Key<>(instanceType + '_' + difficultyLevel + '_' + new Random().nextLong());
    }

    public void killInstance(Key<Instance> instanceKey) {
        Instance instance = instances.get(instanceKey);
        instance.shutdown();
        instances.remove(instanceKey);
    }

    public void playerJoinedToInstance(Id<Player> playerId, Key<Instance> instanceKey) {
        if (playersInstances.containsKey(playerId)) {
            Instance playerPreviousInstance = instances.get(playersInstances.get(playerId));
            playerPreviousInstance.removePlayer(playerId);
        }
        playersInstances.put(playerId, instanceKey);
        instances.get(instanceKey).addPlayer(playerId, gameEvent -> sendMessageToPlayer(playerId, gameEvent));
    }

    private void sendMessageToPlayer(Id<Player> playerId, GameEvent data) {
        connections.get(playerId).onMessage(serializer.serializeMessage(data));
    }

    public void handleMessage(Id<Player> playerId, String message) {
        Key<Instance> instanceKey = playersInstances.get(playerId);
        Object commandToProcess = serializer.parseMessage(message);
        whenTypeOf(commandToProcess)
                .is(CharacterCommand.class)
                .then(command -> instances.get(instanceKey).handleCommand(playerId, command))

                .is(InstanceCommand.class)
                .then(command -> instances.get(instanceKey).handleCommand(command))

                .is(JoinBattleCommand.class)
                .then(command -> {
                    Player.Data playerData = playerService.getPlayer(playerId).getData();
                    Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), command.difficultyLevel);
                    playerService.updatePlayerData(playerId, updatedPlayerData);
                    Key<Instance> newInstanceKey = startInstance(command.map, command.difficultyLevel);
                    sendMessageToPlayer(playerId, new JoinToInstance(newInstanceKey));
                });
    }

    @Override
    public Result verifyConnection(String connectionData) {
        Optional<Id<Player>> playerIdOpt = gate.authenticate(connectionData);
        if (!playerIdOpt.isPresent()) {
            return Result.error("Can not authenticate player");
        }
        return Result.ok();
    }

    @Override
    public void onConnection(ClientConnection<String> clientConnection, String connectionData) {
        Optional<Id<Player>> playerIdOpt = gate.authenticate(connectionData);
        if (!playerIdOpt.isPresent()) {
            throw new RuntimeException("player id should be verified by the verifyConnection method at this point");
        }
        Id<Player> playerId = playerIdOpt.get();

        clientConnection.onOpen(new ContainerConnection(playerId));
        connections.put(playerId, clientConnection);
        playerService.loginPlayer(playerId);
        gate.loginPlayer(playerId);
    }

    public void handleDisconnection(Id<Player> playerId) {
        Key<Instance> instanceKey = gate.playerInstance(playerId);
        Instance instance = instances.get(instanceKey);
        playerService.logoutPlayer(playerId);
        instance.removePlayer(playerId);
        playersInstances.remove(playerId);

        connections.remove(playerId);
        gate.logoutPlayer(playerId);
    }

    public static class JoinToInstance implements GameEvent {
        final Key<Instance> instanceKey;

        JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }
    }

    private final class ContainerConnection implements ServerConnection<String> {
        private final Id<Player> playerId;

        private ContainerConnection(Id<Player> playerId) {
            this.playerId = playerId;
        }

        @Override
        public void send(String message) {
            handleMessage(playerId, message);
        }

        @Override
        public void close() {
            handleDisconnection(playerId);
        }
    }
}
