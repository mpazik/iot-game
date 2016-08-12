package dzida.server.app;

import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.SkillLoader;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDbFactory;
import dzida.server.app.store.memory.SkillStoreInMemory;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Container {
    private final Scheduler scheduler;
    private final InstanceFactory instanceFactory;
    private final Map<Key<Instance>, Instance> instances = new HashMap<>();
    private final Map<Id<Player>, dzida.server.app.network.ConnectionHandler.ConnectionController> connectionControllers = new HashMap<>();
    private final Map<Id<Player>, Key<Instance>> playersInstances = new HashMap<>();

    private final InstanceConnectionHandler connectionHandler;

    Container(PlayerService playerService, Scheduler scheduler, Gate gate) {
        this.scheduler = scheduler;
        StaticDataLoader staticDataLoader = new StaticDataLoader();

        Map<Id<Skill>, Skill> skills = new SkillLoader(staticDataLoader).loadSkills();
        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader));

        instanceFactory = new InstanceFactory(playerService, gate, new SkillStoreInMemory(skills), worldMapStore, new WorldObjectStoreMapDbFactory(), this);
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
            public void handleCommand(Id<Player> playerId, String message) {
                Key<Instance> instanceKey = playersInstances.get(playerId);
                instances.get(instanceKey).receiveMessage(playerId, message);
            }
        };
    }


    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);
        Optional<Instance> instance = instanceFactory.createInstance(instanceKey.getValue(), instanceType, scheduler, difficultyLevel);
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
