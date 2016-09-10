package dzida.server.app;

import co.cask.http.ExceptionHandler;
import co.cask.http.HttpResponder;
import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.achievement.AchievementServer;
import dzida.server.app.achievement.AchievementStore;
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
import dzida.server.app.instance.scenario.ScenarioStore;
import dzida.server.app.leaderboard.Leaderboard;
import dzida.server.app.network.WebSocketServer;
import dzida.server.app.rest.LeaderboardResource;
import dzida.server.app.rest.UserResource;
import dzida.server.app.store.database.AchievementStoreDb;
import dzida.server.app.store.database.ArbiterStoreDb;
import dzida.server.app.store.database.ChatStoreDb;
import dzida.server.app.store.database.FriendsStoreDb;
import dzida.server.app.store.database.InstanceStoreDb;
import dzida.server.app.store.database.ScenarioStoreDb;
import dzida.server.app.store.database.UserStoreDb;
import dzida.server.app.timesync.TimeServiceImpl;
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

        ArbiterStore arbiterStore = new ArbiterStoreDb(connectionProvider);
        ScenarioStore scenarioStore = new ScenarioStoreDb(connectionProvider);
        UserStore userStore = new UserStoreDb(connectionProvider);
        ChatStore chatStore = new ChatStoreDb(connectionProvider);
        InstanceStore instanceStore = new InstanceStoreDb(connectionProvider);
        AchievementStore achievementStore = new AchievementStoreDb(connectionProvider);
        FriendsStore friendsStore = new FriendsStoreDb(connectionProvider);

        int gameServerPort = Configuration.getGameServerPort();
        UserService userService = new UserService(userStore);
        webSocketServer = new WebSocketServer();

        SchedulerImpl scheduler = new SchedulerImpl(webSocketServer.getEventLoop());
        Leaderboard leaderboard = new Leaderboard(userService, scenarioStore);

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        arbiter = new Arbiter(serverDispatcher, scheduler, arbiterStore, scenarioStore, instanceStore);
        TimeSynchroniser timeSynchroniser = new TimeSynchroniser(new TimeServiceImpl());

        Chat chat = new Chat(chatStore);
        arbiter.instanceStartedPublisher.subscribe(instanceServer -> chat.createInstanceChannel(instanceServer.getKey()));
        arbiter.instanceClosedPublisher.subscribe(instanceServer -> chat.closeInstanceChannel(instanceServer.getKey()));

        FriendServer friendServer = new FriendServer(userStore, friendsStore);

        AchievementServer achievementServer = new AchievementServer(achievementStore, leaderboard);
        arbiter.instanceStartedPublisher.subscribe(instanceServer -> {
            instanceServer.userGameEventPublisher.subscribe(achievementServer::processUserGameEvent);
            instanceServer.userCommandPublisher.subscribe(achievementServer::processUserCommand);
            instanceServer.victorySurvivalPublisher.subscribe(achievementServer::processVictorySurvival);
        });


        serverDispatcher.addServer("arbiter", arbiter);
        serverDispatcher.addServer("chat", chat);
        serverDispatcher.addServer("time", timeSynchroniser);
        serverDispatcher.addServer("achievement", achievementServer);
        serverDispatcher.addServer("friends", friendServer);

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
        service.stopAsync();
        webSocketServer.shootDown();
        arbiter.stop();
        connectionManager.close();
    }
}
