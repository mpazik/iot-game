package dzida.server.app;

import co.cask.http.ExceptionHandler;
import co.cask.http.HttpResponder;
import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.analytics.AnalyticsServer;
import dzida.server.app.analytics.AnalyticsStore;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.app.arbiter.ArbiterStore;
import dzida.server.app.chat.Chat;
import dzida.server.app.chat.ChatStore;
import dzida.server.app.database.ConnectionManager;
import dzida.server.app.database.ConnectionProvider;
import dzida.server.app.dispatcher.ServerDispatcher;
import dzida.server.app.friend.FriendServer;
import dzida.server.app.friend.FriendsStore;
import dzida.server.app.instance.InstanceStore;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.parcel.ParcelServer;
import dzida.server.app.parcel.ParcelStore;
import dzida.server.app.rest.UserResource;
import dzida.server.app.store.database.AnalyticsStoreDb;
import dzida.server.app.store.database.ArbiterStoreDb;
import dzida.server.app.store.database.ChatStoreDb;
import dzida.server.app.store.database.FriendsStoreDb;
import dzida.server.app.store.database.InstanceStoreDb;
import dzida.server.app.store.database.ParcelStoreDB;
import dzida.server.app.store.database.UserStoreDb;
import dzida.server.app.timesync.TimeServiceImpl;
import dzida.server.app.timesync.TimeSynchroniser;
import dzida.server.app.user.UserService;
import dzida.server.app.user.UserStore;
import dzida.server.core.profiling.Profilings;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public final class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    private NettyHttpService service;
    private WebSocketServer webSocketServer;
    private ConnectionManager connectionManager;
    private Arbiter arbiter;

    public static void main(String[] args) throws IOException {
        GameServer gameServer = new GameServer();
        gameServer.configureLogger();

        Configuration.print();

        Profilings.printTime("Server start time", gameServer::start);

        gameServer.service.awaitTerminated();
    }

    private void configureLogger() {
        InputStream fis = getClass().getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        ArbiterStore arbiterStore = new ArbiterStoreDb(connectionProvider);
        UserStore userStore = new UserStoreDb(connectionProvider);
        ChatStore chatStore = new ChatStoreDb(connectionProvider);
        InstanceStore instanceStore = new InstanceStoreDb(connectionProvider);
        FriendsStore friendsStore = new FriendsStoreDb(connectionProvider);
        AnalyticsStore analyticsStore = new AnalyticsStoreDb(connectionProvider);
        ParcelStore parcelStore = new ParcelStoreDB(connectionProvider);

        int gameServerPort = Configuration.getGameServerPort();
        UserService userService = new UserService(userStore);
        webSocketServer = new WebSocketServer();

        SchedulerImpl scheduler = new SchedulerImpl(webSocketServer.getEventLoop());

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        arbiter = new Arbiter(serverDispatcher, scheduler, arbiterStore, instanceStore);
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

        Chat chat = new Chat(chatStore);
        arbiter.instanceStartedPublisher.subscribe(instanceServer -> chat.createInstanceChannel(instanceServer.getKey()));
        arbiter.instanceClosedPublisher.subscribe(instanceServer -> chat.closeInstanceChannel(instanceServer.getKey()));

        FriendServer friendServer = new FriendServer(userStore, friendsStore);

        AnalyticsServer analyticsServer = new AnalyticsServer(analyticsStore);
        ParcelServer parcelServer = new ParcelServer(parcelStore);

        serverDispatcher.addServer("arbiter", arbiter);
        serverDispatcher.addServer("chat", chat);
        serverDispatcher.addServer("time", timeSynchroniser);
        serverDispatcher.addServer("friends", friendServer);
        serverDispatcher.addServer("analytics", analyticsServer);
        serverDispatcher.addServer("parcel", parcelServer);

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
                        new UserResource(userService)
                ))
                .build();

        webSocketServer.start(gameServerPort, serverDispatcher);
        arbiter.start();

        log.info("starting");
        service.startAsync();
    }

    private void shutdown() {
        service.stopAsync();
        webSocketServer.shootDown();
        arbiter.stop();
        connectionManager.close();
    }
}
