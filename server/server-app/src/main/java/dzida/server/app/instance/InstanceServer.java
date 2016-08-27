package dzida.server.app.instance;

import com.google.common.util.concurrent.Runnables;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.command.CharacterCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.KillCharacterCommand;
import dzida.server.app.instance.command.SpawnCharacterCommand;
import dzida.server.app.leaderboard.Leaderboard;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.map.descriptor.Survival;
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
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.event.ServerMessage;
import dzida.server.core.scenario.ScenarioEnd;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class InstanceServer implements VerifyingConnectionServer<String, String> {
    private static final Logger log = Logger.getLogger(InstanceServer.class);

    private final Instance instance;
    private final Arbiter arbiter;
    private final JsonProtocol serializer;
    private final StateSynchroniser stateSynchroniser;
    private final UserTokenVerifier userTokenVerifier;
    private final Leaderboard leaderboard;
    private final ScenarioStore scenarioStore;

    private final Key<Instance> instanceKey;
    private final Scenario scenario;
    private final Map<Id<User>, ContainerConnection> connections;
    private Id<Scenario> scenarioId;

    public InstanceServer(Scheduler scheduler, Arbiter arbiter, Leaderboard leaderboard, ScenarioStore scenarioStore, Key<Instance> instanceKey, Scenario scenario) {
        this.arbiter = arbiter;
        this.leaderboard = leaderboard;
        this.scenarioStore = scenarioStore;
        userTokenVerifier = new UserTokenVerifier();
        serializer = InstanceProtocol.createSerializer();
        instance = new Instance(instanceKey.getValue(), scenario, scheduler);
        stateSynchroniser = new StateSynchroniser(instance, scenario);

        this.instanceKey = instanceKey;
        this.scenario = scenario;

        connections = new HashMap<>();
    }

    public void start() {
        instance.subscribeChange(stateSynchroniser::syncStateChange);
        instance.subscribeChange(gameEvent -> {
            if (gameEvent instanceof ScenarioEnd) {
                ScenarioEnd scenarioEnd = (ScenarioEnd) gameEvent;
                scenarioStore.scenarioFinished(scenarioId, scenarioEnd);
                arbiter.instanceFinished(instanceKey);
                if (scenario instanceof Survival) {
                    Survival survival = (Survival) scenario;
                    if (scenarioEnd.resolution == ScenarioEnd.Resolution.Victory) {
                        connections.keySet().forEach(userId -> {
                            leaderboard.notePlayerScore(userId, survival.getDifficultyLevel());
                        });
                    }
                }
            }
        });
        scenarioId = scenarioStore.scenarioStarted(scenario);

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
        if (scenario instanceof Survival && !((Survival) scenario).getAttendees().contains(userId)) {
            return Result.error("Player is not playing the scenario.");
        }
        if (!arbiter.isUserOnInstance(instanceKey, userId)) {
            return Result.error("Player is not assigned to the instance: " + instanceKey);
        }
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));

        ContainerConnection serverConnection = new ContainerConnection(userId, characterId, connector);
        connector.onOpen(serverConnection);
        connections.put(userId, serverConnection);

        Consumer<GameEvent> sendToPlayer = gameEvent -> sendMessageToPlayer(userId, gameEvent);
        PlayerCharacter character = new PlayerCharacter(characterId, userNick);

        instance.handleCommand(new SpawnCharacterCommand(character));
        sendMessageToPlayer(userId, new InstanceProtocol.UserCharacterMessage(characterId, userId, userNick));
        stateSynchroniser.registerCharacter(userId, sendToPlayer);
        log.info("Instance: " + instanceKey + " - user " + userId + " joined \n");
        return Result.ok();
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public void closeInstance() {
        if (!scenarioStore.isScenarioFinished(scenarioId)) {
            scenarioStore.scenarioFinished(scenarioId, new ScenarioEnd(ScenarioEnd.Resolution.Terminated));
        }
    }

    public void disconnectPlayer(Id<User> userId) {
        connections.get(userId).serverClose();
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
        public void send(String message) {
            Object commandToProcess = serializer.parseMessage(message);
            whenTypeOf(commandToProcess)
                    .is(CharacterCommand.class)
                    .then(command -> runInstanceCommand(command.getInstanceCommand(characterId)))
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
