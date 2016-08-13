package dzida.server.app;

import dzida.server.app.command.Command;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Container {
    private final Scheduler scheduler;
    private final Map<Key<Instance>, Instance> instances = new HashMap<>();
    private final Map<Id<Player>, dzida.server.app.network.ConnectionHandler.ConnectionController> connectionControllers = new HashMap<>();
    private final Map<Id<Player>, Key<Instance>> playersInstances = new HashMap<>();
    private final MapDescriptorStore mapDescriptorStore;
    private final PlayerService playerService;

    private final InstanceConnectionHandler connectionHandler;

    Container(PlayerService playerService, Scheduler scheduler, Gate gate) {
        this.scheduler = scheduler;
        this.mapDescriptorStore = new MapDescriptorStore();
        this.playerService = playerService;

        connectionHandler = new InstanceConnectionHandler() {

            @Override
            public void playerConnected(Id<Player> playerId, dzida.server.app.network.ConnectionHandler.ConnectionController connectionController) {
                connectionControllers.put(playerId, connectionController);
                playerService.loginPlayer(playerId);
            }

            @Override
            public void playerDisconnected(Id<Player> playerId) {
                Key<Instance> instanceKey = gate.playerInstance(playerId);
                Instance instance = instances.get(instanceKey);
                playerService.logoutPlayer(playerId);
                instance.removePlayer(playerId);
                playersInstances.remove(playerId);
                connectionControllers.remove(playerId);
            }

            @Override
            public void playerJoinedToInstance(Id<Player> playerId, Key<Instance> instanceKey) {
                if (playersInstances.containsKey(playerId)) {
                    Instance playerPreviousInstance = instances.get(playersInstances.get(playerId));
                    playerPreviousInstance.removePlayer(playerId);
                }
                playersInstances.put(playerId, instanceKey);
                instances.get(instanceKey).addPlayer(playerId, connectionControllers.get(playerId)::send);
            }

            @Override
            public void handleCommand(Id<Player> playerId, Command command) {
                Key<Instance> instanceKey = playersInstances.get(playerId);
                instances.get(instanceKey).handleCommand(playerId, command);
            }
        };
    }


    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);
        String instanceKey1 = instanceKey.getValue();

        Optional<Instance> instance = mapDescriptorStore.getDescriptor(instanceType, difficultyLevel)
                .map(mapDescriptor -> new Instance(instanceKey1, mapDescriptor, scheduler, playerService, this));

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

    public InstanceConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }
}
