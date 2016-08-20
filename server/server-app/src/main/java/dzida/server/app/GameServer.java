package dzida.server.app;

import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.chat.Chat;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.rest.ContainerResource;
import dzida.server.app.rest.LeaderboardResource;
import dzida.server.app.store.mapdb.PlayerStoreMapDb;
import dzida.server.app.timesync.TimeSynchroniser;
import dzida.server.core.player.PlayerService;

import java.io.IOException;

public final class GameServer {

    public static void main(String[] args) throws IOException {
        Configuration.pirnt();

        int gameServerPort = Configuration.getGameServerPort();
        PlayerStoreMapDb playerStore = new PlayerStoreMapDb();
        PlayerService playerService = new PlayerService(playerStore);
        WebSocketServer webSocketServer = new WebSocketServer();
        SchedulerImpl scheduler = new SchedulerImpl(webSocketServer.getEventLoop());

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        Arbiter arbiter = new Arbiter(serverDispatcher, playerService, scheduler);
        Chat chat = new Chat();
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

        serverDispatcher.addServer("arbiter", arbiter);
        serverDispatcher.addServer("chat", chat);
        serverDispatcher.addServer("time", timeSynchroniser);

        webSocketServer.start(gameServerPort, serverDispatcher);
        arbiter.start();

        Leaderboard leaderboard = new Leaderboard(playerStore);
        NettyHttpService service = NettyHttpService.builder()
                .setHost(Configuration.getContainerHost())
                .setPort(Configuration.getContainerRestPort())
                .addHttpHandlers(ImmutableList.of(new ContainerResource(arbiter), new LeaderboardResource(leaderboard, playerStore)))
                .build();

        service.startAsync();
        service.awaitTerminated();
        webSocketServer.shootDown();
    }
}
