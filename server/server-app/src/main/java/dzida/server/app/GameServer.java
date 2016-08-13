package dzida.server.app;

import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.network.ConnectionHandler;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.rest.ContainerResource;
import dzida.server.app.rest.LeaderboardResource;
import dzida.server.app.store.mapdb.PlayerStoreMapDb;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.io.IOException;
import java.util.Optional;

public final class GameServer {

    public static void main(String[] args) throws IOException {
        Configuration.pirnt();

        int gameServerPort = Configuration.getGameServerPort();
        PlayerStoreMapDb playerStore = new PlayerStoreMapDb();
        PlayerService playerService = new PlayerService(playerStore);
        Gate gate = new Gate(playerService, new Key<>(Configuration.getInitialInstances()[0]));
        WebSocketServer webSocketServer = new WebSocketServer();


        Container container = new Container(playerService, new SchedulerImpl(webSocketServer.getEventLoop()), gate);

        ConnectionHandler<Id<Player>> connectionHandler = new ConnectionHandlerImpl(gate, container.getConnectionHandler());
        webSocketServer.start(gameServerPort, connectionHandler);

        for (String instance : Configuration.getInitialInstances()) {
            container.startInstance(instance, null);
        }

        Leaderboard leaderboard = new Leaderboard(playerStore);
        NettyHttpService service = NettyHttpService.builder()
                .setHost(Configuration.getContainerHost())
                .setPort(Configuration.getContainerRestPort())
                .addHttpHandlers(ImmutableList.of(new ContainerResource(gate), new LeaderboardResource(leaderboard, playerStore)))
                .build();

        service.startAsync();
        service.awaitTerminated();
        webSocketServer.shootDown();
    }

    private static final class ConnectionHandlerImpl implements ConnectionHandler<Id<Player>> {
        private final Gate gate;
        private final InstanceConnectionHandler instanceConnectionHandler;
        private final CommandParser commandParser;

        private ConnectionHandlerImpl(Gate gate, InstanceConnectionHandler instanceConnectionHandler) {
            this.gate = gate;
            this.instanceConnectionHandler = instanceConnectionHandler;
            gate.subscribePlayerJoinedToInstance(instanceConnectionHandler::playerJoinedToInstance);
            commandParser = new CommandParser();
        }

        @Override
        public Optional<Id<Player>> authenticateUser(String authToken) {
            return gate.authenticate(authToken);
        }

        @Override
        public void handleConnection(Id<Player> playerId, ConnectionController connectionController) {
            instanceConnectionHandler.playerConnected(playerId, connectionController);
            gate.loginPlayer(playerId);
        }

        @Override
        public void handleMessage(Id<Player> playerId, String packet) {
            commandParser.readPacket(packet).forEach(command -> instanceConnectionHandler.handleCommand(playerId, command));
        }

        @Override
        public void handleDisconnection(Id<Player> playerId) {
            instanceConnectionHandler.playerDisconnected(playerId);
            gate.logoutPlayer(playerId);
        }
    }
}
