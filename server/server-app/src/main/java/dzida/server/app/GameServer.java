package dzida.server.app;

import co.cask.http.ExceptionHandler;
import co.cask.http.HttpResponder;
import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.chat.Chat;
import dzida.server.app.database.ConnectionManager;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.rest.LeaderboardResource;
import dzida.server.app.rest.UserResource;
import dzida.server.app.store.database.UserStoreDb;
import dzida.server.app.timesync.TimeSynchroniser;
import dzida.server.app.user.UserService;
import dzida.server.app.user.UserStore;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.io.IOException;

public final class GameServer {

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();

        Configuration.print();

        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.connect();
        ConnectionProvider connectionProvider = connectionManager.getConnectionProvider();

        UserStore userStore = new UserStoreDb(connectionProvider);
        UserService userService = new UserService(userStore);

        int gameServerPort = Configuration.getGameServerPort();
        WebSocketServer webSocketServer = new WebSocketServer();
        SchedulerImpl scheduler = new SchedulerImpl(webSocketServer.getEventLoop());

        Leaderboard leaderboard = new Leaderboard();
        ServerDispatcher serverDispatcher = new ServerDispatcher();
        Chat chat = new Chat();
        Arbiter arbiter = new Arbiter(serverDispatcher, chat, scheduler, leaderboard);
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

        serverDispatcher.addServer("arbiter", arbiter);
        serverDispatcher.addServer("chat", chat);
        serverDispatcher.addServer("time", timeSynchroniser);

        webSocketServer.start(gameServerPort, serverDispatcher);
        arbiter.start();

        long elapsedTime = System.nanoTime() - start;
        System.out.println("Started in: " + ((double) (elapsedTime / 1000) / 1000) + "ms");

        NettyHttpService service = NettyHttpService.builder()
                .setHost(Configuration.getContainerHost())
                .setPort(Configuration.getContainerRestPort())
                .setExceptionHandler(new ExceptionHandler() {
                    @Override
                    public void handle(Throwable t, HttpRequest request, HttpResponder responder) {
                        t.printStackTrace();
                        super.handle(t, request, responder);
                    }
                })
                .addHttpHandlers(ImmutableList.of(
                        new LeaderboardResource(leaderboard),
                        new UserResource(userService)
                ))
                .build();

        service.startAsync();
        service.awaitTerminated();
        webSocketServer.shootDown();
        connectionManager.close();
    }
}
