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
import dzida.server.core.profiling.Profilings;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.io.IOException;

public final class GameServer {
    private static final Logger log = Logger.getLogger(GameServer.class);

    private NettyHttpService service;
    private WebSocketServer webSocketServer;
    private ConnectionManager connectionManager;
    private Arbiter arbiter;

    public static void main(String[] args) throws IOException {
        GameServer gameServer = new GameServer();

        Configuration.print();

        Profilings.printTime("Server start time", gameServer::start);

        gameServer.service.awaitTerminated();
    }

    private void start() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        connectionManager = new ConnectionManager();
        connectionManager.connect();
        ConnectionProvider connectionProvider = connectionManager.getConnectionProvider();

        UserStore userStore = new UserStoreDb(connectionProvider);
        UserService userService = new UserService(userStore);

        webSocketServer = new WebSocketServer();
        int gameServerPort = Configuration.getGameServerPort();
        SchedulerImpl scheduler = new SchedulerImpl(webSocketServer.getEventLoop());

        Leaderboard leaderboard = new Leaderboard();
        ServerDispatcher serverDispatcher = new ServerDispatcher();
        Chat chat = new Chat();
        arbiter = new Arbiter(serverDispatcher, chat, scheduler, leaderboard);
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

        serverDispatcher.addServer("arbiter", arbiter);
        serverDispatcher.addServer("chat", chat);
        serverDispatcher.addServer("time", timeSynchroniser);

        service = NettyHttpService.builder()
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

        webSocketServer.start(gameServerPort, serverDispatcher);
        arbiter.start();

        log.info("starting");
        service.startAsync();
    }

    private void shutdown() {
        arbiter.close();
        service.stopAsync();
        webSocketServer.shootDown();
        connectionManager.close();
    }
}
