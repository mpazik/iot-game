package dzida.server.app.arbiter;

import com.google.common.collect.ImmutableList;
import dzida.server.app.Configuration;
import dzida.server.app.Leaderboard;
import dzida.server.app.chat.Chat;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.instance.Instance;
import dzida.server.app.instance.InstanceServer;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.app.user.EncryptedLoginToken;
import dzida.server.app.user.LoginToken;
import dzida.server.app.user.User;
import dzida.server.app.user.UserTokenVerifier;
import dzida.server.core.Scheduler;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import org.apache.log4j.Logger;

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
    private static final Logger log = Logger.getLogger(Arbiter.class);

    private final ServerDispatcher serverDispatcher;
    private final Chat chat;
    private final Scheduler scheduler;
    private final Leaderboard leaderboard;
    private final JsonProtocol arbiterProtocol;
    private final UserTokenVerifier userTokenVerifier;

    private final Map<Id<User>, Key<Instance>> usersInstances;
    private final Map<Key<Instance>, InstanceServer> instances;
    private final Set<Key<Instance>> initialInstances;
    private final Key<Instance> defaultInstance;
    private final Set<Key<Instance>> instancesToShutdown;
    private final Set<Id<User>> connectedUsers;

    public Arbiter(ServerDispatcher serverDispatcher, Chat chat, Scheduler scheduler, Leaderboard leaderboard) {
        this.serverDispatcher = serverDispatcher;
        this.chat = chat;
        this.scheduler = scheduler;
        this.leaderboard = leaderboard;
        arbiterProtocol = ArbiterProtocol.createSerializer();
        userTokenVerifier = new UserTokenVerifier();

        usersInstances = new HashMap<>();
        instances = new HashMap<>();
        initialInstances = ImmutableList.copyOf(Configuration.getInitialInstances())
                .stream()
                .map((Function<String, Key<Instance>>) Key::new)
                .collect(Collectors.toSet());
        instancesToShutdown = new HashSet<>();
        defaultInstance = new Key<>(Configuration.getInitialInstances()[0]);
        connectedUsers = new HashSet<>();
    }

    public void start() {
        log.info("Arbiter: started system");
        initialInstances.forEach(instanceKey -> {
            startInstance(instanceKey.getValue(), null);
        });
    }

    public Key<Instance> startInstance(String instanceType, Integer difficultyLevel) {
        Key<Instance> instanceKey = generateInstanceKey(instanceType, difficultyLevel);

        String instanceKeyValue = instanceKey.getValue();
        InstanceServer instanceServer = new InstanceServer(scheduler, this, leaderboard, instanceKey, instanceType, difficultyLevel);
        instanceServer.start();

        serverDispatcher.addServer(instanceKeyValue, instanceServer);
        instances.put(instanceKey, instanceServer);
        chat.createInstanceChannel(instanceKey);
        log.info("Arbiter: started instance: " + instanceKey);
        return instanceKey;
    }

    private Key<Instance> generateInstanceKey(String instanceType, Integer difficultyLevel) {
        if (difficultyLevel == null) {
            return new Key<>(instanceType);
        }
        return new Key<>(instanceType + '_' + difficultyLevel + '_' + new Random().nextInt(10000000));
    }

    @Override
    public Result onConnection(Connector<String> connector, String userToken) {
        Optional<LoginToken> loginToken = userTokenVerifier.verifyToken(new EncryptedLoginToken(userToken));
        if (!loginToken.isPresent()) {
            return Result.error("Login to is invalid");
        }

        Id<User> userId = loginToken.get().userId;
        if (connectedUsers.contains(userId)) {
            return Result.error("User is already logged in.");
        }
        connectedUsers.add(userId);

        ArbiterConnection arbiterConnection = new ArbiterConnection(userId, connector);
        connector.onOpen(arbiterConnection);
        arbiterConnection.movePlayerToInstance(defaultInstance);
        return Result.ok();
    }

    public boolean isUserOnInstance(Key<Instance> instanceKey, Id<User> userId) {
        return usersInstances.get(userId).equals(instanceKey);
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
        log.info("Arbiter: shutdown instance: " + instanceKey);
    }

    private final class ArbiterConnection implements ServerConnection<String> {
        private final Id<User> userId;
        private final Connector<String> connector;

        ArbiterConnection(Id<User> userId, Connector<String> connector) {
            this.userId = userId;
            this.connector = connector;
        }

        @Override
        public void send(String data) {
            Object message = arbiterProtocol.parseMessage(data);
            whenTypeOf(message)
                    .is(JoinBattleCommand.class)
                    .then(command -> {
                        removePlayerFromLastInstance();
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
            usersInstances.put(userId, newInstanceKey);
            connector.onMessage(arbiterProtocol.serializeMessage(new JoinToInstance(newInstanceKey)));
            log.info("Arbiter: user: " + userId + " assigned to instance: " + newInstanceKey);
        }

        private void removePlayerFromLastInstance() {
            Key<Instance> lastInstanceKey = usersInstances.get(userId);
            if (lastInstanceKey == null) return;
            InstanceServer instanceServer = instances.get(lastInstanceKey);
            instanceServer.disconnectPlayer(userId);
            log.info("Arbiter: user: " + userId + " removed from instance: " + lastInstanceKey);
            tryToKillInstance(lastInstanceKey);
        }

        private void tryToKillInstance(Key<Instance> instanceKey) {
            if (instancesToShutdown.contains(instanceKey) && instances.get(instanceKey).isEmpty()) {
                shutdownInstanced(instanceKey);
            }
        }

        @Override
        public void close() {
            connectedUsers.remove(userId);
            usersInstances.remove(userId);
        }
    }
}
