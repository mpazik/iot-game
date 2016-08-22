package dzida.server.app.instance;

import com.google.common.util.concurrent.Runnables;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.command.CharacterCommand;
import dzida.server.app.instance.command.InstanceCommand;
import dzida.server.app.instance.command.KillCharacterCommand;
import dzida.server.app.instance.command.SpawnCharacterCommand;
import dzida.server.app.map.descriptor.MapDescriptorStore;
import dzida.server.app.map.descriptor.Scenario;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.app.user.EncryptedLoginToken;
import dzida.server.app.user.LoginToken;
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
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.scenario.ScenarioEnd;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class InstanceServer implements VerifyingConnectionServer<String, String> {
    private final Instance instance;
    private final PlayerService playerService;
    private final Arbiter arbiter;
    private final JsonProtocol serializer;
    private final StateSynchroniser stateSynchroniser;
    private final UserTokenVerifier userTokenVerifier;

    private final Key<Instance> instanceKey;
    private final Map<Id<Player>, ContainerConnection> connections;

    public InstanceServer(PlayerService playerService, Scheduler scheduler, Arbiter arbiter, Key<Instance> instanceKey, String instanceType, Integer difficultyLevel) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        userTokenVerifier = new UserTokenVerifier();

        serializer = InstanceProtocol.createSerializer();
        Scenario scenario = new MapDescriptorStore().getScenario(instanceType, difficultyLevel);
        instance = new Instance(instanceKey.getValue(), scenario, scheduler);
        stateSynchroniser = new StateSynchroniser(instance, scenario);

        this.instanceKey = instanceKey;
        connections = new HashMap<>();

        instance.subscribeChange(stateSynchroniser::syncStateChange);
        instance.subscribeChange(gameEvent -> {
            if (gameEvent instanceof ScenarioEnd) {
                ScenarioEnd scenarioEnd = (ScenarioEnd) gameEvent;
                arbiter.endOfScenario(instanceKey);
                if (scenarioEnd.resolution == ScenarioEnd.Resolution.Victory) {
                    connections.keySet().forEach(playerId -> {
                        Player.Data player = playerService.getPlayer(playerId).getData();
                        if (scenarioEnd.difficultyLevel > player.getHighestDifficultyLevel()) {
                            Player.Data newPlayerData = new Player.Data(player.getNick(), scenarioEnd.difficultyLevel);
                            playerService.updatePlayerData(playerId, newPlayerData);
                        }
                    });
                }
            }
        });

        instance.start();
    }

    private void sendMessageToPlayer(Id<Player> playerId, GameEvent data) {
        connections.get(playerId).serverSend(serializer.serializeMessage(data));
    }

    @Override
    public Result onConnection(Connector<String> connector, String userToken) {
        Optional<LoginToken> loginToken = userTokenVerifier.verifyToken(new EncryptedLoginToken(userToken));
        if (!loginToken.isPresent()) {
            return Result.error("Login to is invalid");
        }

        Optional<Id<Player>> playerIdOpt = arbiter.authenticate(instanceKey, loginToken.get().nick);
        if (!playerIdOpt.isPresent()) {
            return Result.error("Player is not assigned to the instance: " + instanceKey);
        }
        Id<Player> playerId = playerIdOpt.get();
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));

        ContainerConnection serverConnection = new ContainerConnection(playerId, characterId, connector);
        connector.onOpen(serverConnection);
        connections.put(playerId, serverConnection);

        Consumer<GameEvent> sendToPlayer = gameEvent -> sendMessageToPlayer(playerId, gameEvent);
        Player playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);

        instance.handleCommand(new SpawnCharacterCommand(character));
        stateSynchroniser.registerCharacter(playerId, sendToPlayer);
        stateSynchroniser.sendInitialPacket(characterId, playerId, playerEntity);
        System.out.printf("Instance: %s - player %s joined \n", instanceKey, playerId);
        return Result.ok();
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public void disconnectPlayer(Id<Player> playerId) {
        connections.get(playerId).serverClose();
    }

    private final class ContainerConnection implements ServerConnection<String> {
        private final Id<Player> playerId;
        private final Id<Character> characterId;
        private final Connector<String> connector;

        private ContainerConnection(Id<Player> playerId, Id<Character> characterId, Connector<String> connector) {
            this.playerId = playerId;
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
                sendMessageToPlayer(playerId, new ServerMessage(error.getMessage()));
            });
        }

        @Override
        public void close() {
            stateSynchroniser.unregisterListener(playerId);
            instance.handleCommand(new KillCharacterCommand(characterId));
            System.out.printf("Instance: %s - player %s quit \n", instanceKey, playerId);

            connections.remove(playerId);
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
