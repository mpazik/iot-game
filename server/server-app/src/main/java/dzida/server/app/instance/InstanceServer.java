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
    private final Map<Id<Player>, Connector<String>> connections = new HashMap<>();
    private final PlayerService playerService;
    private final Arbiter arbiter;
    private final JsonProtocol serializer;
    private final Key<Instance> instanceKey;
    private final StateSynchroniser stateSynchroniser;

    public InstanceServer(PlayerService playerService, Scheduler scheduler, Arbiter arbiter, Key<Instance> instanceKey, String instanceType, Integer difficultyLevel) {
        this.playerService = playerService;
        this.arbiter = arbiter;
        this.instanceKey = instanceKey;

        serializer = InstanceProtocol.createSerializer();
        Scenario scenario = new MapDescriptorStore().getScenario(instanceType, difficultyLevel);
        instance = new Instance(instanceKey.getValue(), scenario, scheduler);
        stateSynchroniser = new StateSynchroniser(instance, scenario);
        instance.subscribeChange(stateSynchroniser::syncStateChange);
        instance.subscribeChange(gameEvent -> {
            if (gameEvent instanceof ScenarioEnd && ((ScenarioEnd) gameEvent).resolution == ScenarioEnd.Resolution.Victory) {
                connections.keySet().forEach(playerId -> {
                    Player.Data player = playerService.getPlayer(playerId).getData();
                    int scenarioDifficultyLevel = ((ScenarioEnd) gameEvent).difficultyLevel;
                    if (scenarioDifficultyLevel > player.getHighestDifficultyLevel()) {
                        Player.Data newPlayerData = new Player.Data(player.getNick(), scenarioDifficultyLevel, player.getLastDifficultyLevel());
                        playerService.updatePlayerData(playerId, newPlayerData);
                    }
                });
            }
        });
        instance.start();
    }

    private void sendMessageToPlayer(Id<Player> playerId, GameEvent data) {
        connections.get(playerId).onMessage(serializer.serializeMessage(data));
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
        Id<Character> characterId = new Id<>((int) Math.round((Math.random() * 100000)));

        connector.onOpen(new ContainerConnection(playerId, characterId));
        connections.put(playerId, connector);
        playerService.loginPlayer(playerId);

        Consumer<GameEvent> sendToPlayer = gameEvent -> sendMessageToPlayer(playerId, gameEvent);
        Player playerEntity = playerService.getPlayer(playerId);
        String nick = playerEntity.getData().getNick();
        PlayerCharacter character = new PlayerCharacter(characterId, nick, playerId);

        instance.handleCommand(new SpawnCharacterCommand(character));
        stateSynchroniser.registerCharacter(playerId, sendToPlayer);
        stateSynchroniser.sendInitialPacket(characterId, playerId, playerEntity);
        System.out.printf("Instance: %s - player %s joined", instanceKey, playerId);
    }

    private final class ContainerConnection implements ServerConnection<String> {
        private final Id<Player> playerId;
        private final Id<Character> characterId;

        private ContainerConnection(Id<Player> playerId, Id<Character> characterId) {
            this.playerId = playerId;
            this.characterId = characterId;
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
            playerService.logoutPlayer(playerId);
            stateSynchroniser.unregisterListener(playerId);
            instance.handleCommand(new KillCharacterCommand(characterId));
            System.out.printf("Instance: %s - player %s quit", instanceKey, playerId);

            connections.remove(playerId);
        }
    }
}
