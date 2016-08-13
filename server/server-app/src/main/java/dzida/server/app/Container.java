package dzida.server.app;

import dzida.server.app.command.Command;
import dzida.server.app.command.InstanceCommand;
import dzida.server.app.command.JoinBattleCommand;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;
import static dzida.server.app.Serializer.getSerializer;

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
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

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
            public void handleCommand(Id<Player> playerId, Command commandToProcess) {
                Key<Instance> instanceKey = playersInstances.get(playerId);
                whenTypeOf(commandToProcess)
                        .is(InstanceCommand.class)
                        .then(command -> instances.get(instanceKey).handleCommand(playerId, command))

                        .is(JoinBattleCommand.class)
                        .then(command -> {
                            Player.Data playerData = playerService.getPlayer(playerId).getData();
                            Player.Data updatedPlayerData = new Player.Data(playerData.getNick(), playerData.getHighestDifficultyLevel(), command.difficultyLevel);
                            playerService.updatePlayerData(playerId, updatedPlayerData);
                            Key<Instance> newInstanceKey = startInstance(command.map, command.difficultyLevel);
                            sendMessageToPlayer(playerId, new JoinToInstance(newInstanceKey));
                        })

                        .is(TimeSynchroniser.TimeSyncRequest.class)
                        .then(command -> {
                            TimeSynchroniser.TimeSyncResponse timeSyncResponse = timeSynchroniser.timeSync(command);
                            sendMessageToPlayer(playerId, timeSyncResponse);
                        });
            }

            public void sendMessageToPlayer(Id<Player> playerId, GameEvent data) {
                connectionControllers.get(playerId).send(getSerializer().toJson(Collections.singleton(new Packet(data.getId(), data))));
            }
        };
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

    public InstanceConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    private static class JoinToInstance implements GameEvent {
        final Key<Instance> instanceKey;

        JoinToInstance(Key<Instance> instanceKey) {
            this.instanceKey = instanceKey;
        }

        @Override
        public int getId() {
            return InstanceCreated;
        }
    }
}
