package dzida.server.app.instance;

import com.google.common.util.concurrent.Runnables;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.command.CharacterCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class InstanceServer implements VerifyingConnectionServer<String, String> {
    private final Instance instance;
    private final Map<Id<Player>, Connector<String>> connections = new HashMap<>();
    private final PlayerService playerService;
    private final Arbiter arbiter;
    private final JsonProtocol serializer;
    private final Key<Instance> instanceKey;

    public InstanceServer(PlayerService playerService, Scheduler scheduler, Arbiter arbiter, Key<Instance> instanceKey, String instanceType, Integer difficultyLevel) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.instanceKey = instanceKey;

        serializer = InstanceProtocol.createSerializer();
        Optional<Scenario> descriptor = new MapDescriptorStore().getDescriptor(instanceType, difficultyLevel);
        if (!descriptor.isPresent()) {
            throw new RuntimeException("map descriptor is not valid: " + instanceType);
        }
        instance = new Instance(instanceKey.getValue(), descriptor.get(), scheduler, playerService);
        instance.start();
    }

    private void sendMessageToPlayer(Id<Player> playerId, GameEvent data) {
        connections.get(playerId).onMessage(serializer.serializeMessage(data));
    }

    public void handleMessage(Id<Player> playerId, String message) {
        Object commandToProcess = serializer.parseMessage(message);
        whenTypeOf(commandToProcess)
                .is(CharacterCommand.class)
                .then(command -> {
                    Result result = instance.handleCommand(playerId, command);
                    result.consume(Runnables.doNothing(), error -> {
                        sendMessageToPlayer(playerId, new ServerMessage(error.getMessage()));
                    });
                    instance.handleCommand(playerId, command);
                })

                .is(InstanceCommand.class)
                .then(command -> {
                    Result result = instance.handleCommand(command);
                    result.consume(Runnables.doNothing(), error -> {
                        sendMessageToPlayer(playerId, new ServerMessage(error.getMessage()));
                    });
                });
    }

    @Override
    public Result verifyConnection(String connectionData) {
        Optional<Id<Player>> playerIdOpt = arbiter.authenticate(instanceKey, connectionData);
        if (!playerIdOpt.isPresent()) {
            return Result.error("Can not authenticate player");
        }
        return Result.ok();
    }

    @Override
    public void onConnection(Connector<String> connector, String connectionData) {
        Optional<Id<Player>> playerIdOpt = arbiter.authenticate(instanceKey, connectionData);
        if (!playerIdOpt.isPresent()) {
            throw new RuntimeException("player id should be verified by the verifyConnection method at this point");
        }
        Id<Player> playerId = playerIdOpt.get();

        connector.onOpen(new ContainerConnection(playerId));
        connections.put(playerId, connector);
        playerService.loginPlayer(playerId);

        instance.addPlayer(playerId, gameEvent -> sendMessageToPlayer(playerId, gameEvent));
    }

    public void handleDisconnection(Id<Player> playerId) {
        playerService.logoutPlayer(playerId);
        instance.removePlayer(playerId);

        connections.remove(playerId);
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
