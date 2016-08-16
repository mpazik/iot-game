package dzida.server.app;

import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.dispatcher.ClientConnection;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.network.Connection;
import dzida.server.app.network.ConnectionHandler;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.rest.ContainerResource;
import dzida.server.app.rest.LeaderboardResource;
import dzida.server.app.store.mapdb.PlayerStoreMapDb;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.PlayerService;

import java.io.IOException;

public final class GameServer {

    public static void main(String[] args) throws IOException {
        Configuration.pirnt();

        int gameServerPort = Configuration.getGameServerPort();
        PlayerStoreMapDb playerStore = new PlayerStoreMapDb();
        PlayerService playerService = new PlayerService(playerStore);
        Gate gate = new Gate(playerService, new Key<>(Configuration.getInitialInstances()[0]));
        WebSocketServer webSocketServer = new WebSocketServer();

        Container container = new Container(playerService, new SchedulerImpl(webSocketServer.getEventLoop()), gate);
        ServerDispatcher serverDispatcher = new ServerDispatcher();
        serverDispatcher.addServer(container);

        ConnectionHandler connectionHandler = new ConnectionHandlerImpl(serverDispatcher);
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

    private static final class ConnectionHandlerImpl implements ConnectionHandler {
        private final ServerDispatcher serverDispatcher;

        private ConnectionHandlerImpl(ServerDispatcher serverDispatcher) {
            this.serverDispatcher = serverDispatcher;
        }

        public void handleConnection(int connectionId, Connection connectionController) {
            serverDispatcher.handleConnection(connectionId, new ClientConnection() {
                @Override
                public void disconnect() {
                    connectionController.disconnect();
                }

                @Override
                public void send(String data) {
                    connectionController.send(data);
                }
            });
        }

        @Override
        public void handleMessage(int connectionId, String message) {
            serverDispatcher.handleMessage(connectionId, message);
        }

        @Override
        public void handleDisconnection(int connectionId) {
            serverDispatcher.handleDisconnection(connectionId);
        }
    }
}
