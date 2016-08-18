package dzida.server.app;

import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.chat.Chat;
import dzida.server.app.dispatcher.ServerDispatcher;
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
        Chat chat = new Chat();

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        serverDispatcher.addServer("instance", container);
        serverDispatcher.addServer("chat", chat);

        webSocketServer.start(gameServerPort, serverDispatcher);

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
}
