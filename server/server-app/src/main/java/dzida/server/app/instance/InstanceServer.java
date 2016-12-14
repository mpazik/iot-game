package dzida.server.app.instance;

import com.google.common.util.concurrent.Runnables;
import dzida.server.app.Scheduler;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.basic.Publisher;
import dzida.server.app.basic.Result;
import dzida.server.app.basic.connection.Connector;
import dzida.server.app.basic.connection.ServerConnection;
import dzida.server.app.basic.connection.VerifyingConnectionServer;
import dzida.server.app.basic.entity.Id;
import dzida.server.app.basic.entity.Key;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.KillCharacterCommand;
import dzida.server.app.instance.command.SpawnCharacterCommand;
import dzida.server.app.instance.event.CharacterEvent;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.event.ServerMessage;
import dzida.server.app.instance.position.event.CharacterMoved;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.app.user.EncryptedLoginToken;
import dzida.server.app.user.LoginToken;
import dzida.server.app.user.User;
import dzida.server.app.user.UserTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class InstanceServer implements VerifyingConnectionServer<String, String> {
    private static final Logger log = LoggerFactory.getLogger(InstanceServer.class);

    public final Publisher<UserMessage.UserGameEvent> userGameEventPublisher = new Publisher<>();
    public final Publisher<UserMessage.UserCommand> userCommandPublisher = new Publisher<>();

    private final Instance instance;
    private final InstanceStore instanceStore;
    private final Arbiter arbiter;
    private final JsonProtocol serializer;
    private final StateSynchroniser stateSynchroniser;
    private final UserTokenVerifier userTokenVerifier;

    private final Key<Instance> instanceKey;
    private final Map<Id<User>, ContainerConnection> connections = new HashMap<>();
    private final Map<Id<Character>, Id<User>> userIds = new HashMap<>();

    public InstanceServer(Scheduler scheduler, InstanceStore instanceStore, Arbiter arbiter, Key<Instance> instanceKey, Scenario scenario) {
        this.instanceStore = instanceStore;
        this.arbiter = arbiter;
        userTokenVerifier = new UserTokenVerifier();

        serializer = JsonProtocol.create(CharacterCommand.classes, InstanceEvent.classes);
        instance = new Instance(instanceKey.getValue(), scenario, scheduler);
        stateSynchroniser = new StateSynchroniser(instance, scenario);

        this.instanceKey = instanceKey;
    }

    public void start() {
        instance.subscribeChange(stateSynchroniser::syncStateChange);
        instance.subscribeChange(gameEvent -> {
            if (gameEvent instanceof CharacterMoved) {
                return;
            }
            instanceStore.saveEvent(instanceKey, gameEvent);
        });
        instance.subscribeChange(gameEvent -> {
            if (gameEvent instanceof CharacterEvent) {
                CharacterEvent characterEvent = (CharacterEvent) gameEvent;
                if (!userIds.containsKey(characterEvent.getCharacterId())) {
                    return; // Character is not an user's character (It's a bot)
                }
                Id<User> userId = userIds.get(characterEvent.getCharacterId());
                userGameEventPublisher.notify(new UserMessage.UserGameEvent(userId, characterEvent));
            }
        });
        instance.start();
    }

    private void sendMessageToPlayer(Id<User> userId, GameEvent data) {
        connections.get(userId).serverSend(serializer.serializeMessage(data));
    }

    @Override
    public Result onConnection(Connector<String> connector, String userToken) {
        Optional<LoginToken> loginToken = userTokenVerifier.verifyToken(new EncryptedLoginToken(userToken));
        if (!loginToken.isPresent()) {
            return Result.error("Login to is invalid");
        }

        Id<User> userId = loginToken.get().userId;
        String userNick = loginToken.get().nick;
        if (connections.containsKey(userId)) {
            return Result.error("User is already logged in.");
        }
        if (!arbiter.isUserOnInstance(instanceKey, userId)) {
            return Result.error("Player is not assigned to the instance: " + instanceKey);
        }
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));

        ContainerConnection serverConnection = new ContainerConnection(userId, characterId, connector);
        connector.onOpen(serverConnection);
        connections.put(userId, serverConnection);
        userIds.put(characterId, userId);

        Consumer<GameEvent> sendToPlayer = gameEvent -> sendMessageToPlayer(userId, gameEvent);
        PlayerCharacter character = new PlayerCharacter(characterId, userId);

        instance.handleCommand(new SpawnCharacterCommand(character));
        sendMessageToPlayer(userId, new Instance.UserCharacter(characterId, userId, userNick));
        stateSynchroniser.registerCharacter(userId, sendToPlayer);
        log.info("Instance: " + instanceKey + " - user " + userId + " joined \n");
        return Result.ok();
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public void closeInstance() {
    }

    public void disconnectPlayer(Id<User> userId) {
        connections.get(userId).serverClose();
    }

    public Key<Instance> getKey() {
        return instanceKey;
    }

    private final class ContainerConnection implements ServerConnection<String> {
        private final Id<User> userId;
        private final Id<Character> characterId;
        private final Connector<String> connector;

        private ContainerConnection(Id<User> userId, Id<Character> characterId, Connector<String> connector) {
            this.userId = userId;
            this.characterId = characterId;
            this.connector = connector;
        }

        @Override
        public void send(String data) {
            Object commandToProcess = serializer.parseMessage(data);
            whenTypeOf(commandToProcess)
                    .is(CharacterCommand.class)
                    .then(command -> {
                        runInstanceCommand(command.getInstanceCommand(characterId));
                        userCommandPublisher.notify(new UserMessage.UserCommand(userId, command));
                    })
                    .is(InstanceCommand.class)
                    .then(this::runInstanceCommand);
        }

        public void runInstanceCommand(InstanceCommand command) {
            Result result = instance.handleCommand(command);
            result.consume(Runnables.doNothing(), error -> {
                sendMessageToPlayer(userId, new ServerMessage(error.getMessage()));
            });
        }

        @Override
        public void close() {
            stateSynchroniser.unregisterListener(userId);
            instance.handleCommand(new KillCharacterCommand(characterId));
            log.info("Instance: " + instanceKey + " - user " + userId + " quit");

            connections.remove(userId);
            userIds.remove(characterId);
        }

        public void serverSend(String data) {
            connector.onMessage(data);
        }

        public void serverClose() {
            close();
            connector.onClose();
        }
    }
}
