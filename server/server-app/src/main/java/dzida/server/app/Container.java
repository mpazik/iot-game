package dzida.server.app;

import dzida.server.app.store.http.WorldMapStoreHttp;
import dzida.server.app.store.http.loader.SkillLoader;
import dzida.server.app.store.http.loader.StaticDataLoader;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.store.mapdb.PlayerStoreMapDb;
import dzida.server.app.store.mapdb.WorldObjectStoreMapDbFactory;
import dzida.server.app.store.memory.SkillStoreInMemory;
import dzida.server.core.basic.Error;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.entity.Entity;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;
import dzida.server.core.skill.Skill;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import lombok.Value;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Container {
    private final URI address;
    private final EventLoopGroup bossGroup;
    private final InstanceFactory instanceFactory;
    private final PlayerService playerService;
    private final Map<String, InstanceData> instances = new HashMap<>();
    private final Map<ChannelId, Player.Id> players = new HashMap<>();
    private int nextPort;

    Container(int startPort, URI address, PlayerStoreMapDb playerStore) {
        bossGroup = new NioEventLoopGroup();
        playerService = new PlayerService(playerStore);
        StaticDataLoader staticDataLoader = new StaticDataLoader();

        Map<Id<Skill>, Skill> skills = new SkillLoader(staticDataLoader).loadSkills();
        WorldMapStoreHttp worldMapStore = new WorldMapStoreHttp(new WorldMapLoader(staticDataLoader));

        instanceFactory = new InstanceFactory(playerService, new Arbiter(this), new SkillStoreInMemory(skills), worldMapStore, new WorldObjectStoreMapDbFactory());
        nextPort = startPort;
        this.address = address;
    }

    public Result canPlayerLogIn(String nick) {
        // since there is auto account creator if player does not exist it can log in.
        Boolean isPlayerPlaying = playerService.findPlayer(nick).map(playerService::isPlayerPlaying).orElse(false);
        if (isPlayerPlaying) {
            return Result.error(new Error("Player is already logged in."));
        }
        return Result.ok();
    }

    public void startInstance(String instanceKey, String instanceType, StartInstanceCallback callback, Integer difficultyLevel) {
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        EventLoop eventLoop = workerGroup.next();
        Optional<Instance> instance = instanceFactory.createInstance(instanceKey, instanceType, eventLoop, difficultyLevel);
        if (!instance.isPresent()) {
            System.err.println("map descriptor is not valid: " + instanceType);
            return;
        }
        instance.get().start();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerHandler(new ConnectionHandler(instance.get())));
                        }
                    });
            int instancePort = nextPort;
            Channel instanceChannel = b.bind(instancePort).sync().channel();
            instances.put(instanceKey, new InstanceData(instanceKey, instanceChannel, workerGroup));
            URI instanceUri = UriBuilder.fromUri(address).port(instancePort).build();
            callback.call(instanceUri);
            this.nextPort += 1;
            System.out.println(String.format("Started instance:%s on port:%s", instanceKey, instancePort));
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    public void killInstance(String instanceKey) {
        InstanceData instance = instances.get(instanceKey);
        instance.getWorkerGroup().shutdownGracefully();
        System.out.println("Killed instance: " + instance.getInstanceKey());
    }

    public Future<?> shutdownGracefully() {
        // it won't be closed because instances are blocking.
        instances.values().stream().forEach(instance -> {
            try {
                instance.getInstanceChannel().closeFuture().sync();
                instance.getWorkerGroup().shutdownGracefully();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return bossGroup.shutdownGracefully();
    }

    public interface StartInstanceCallback {
        void call(URI address);
    }

    @Value
    private static final class InstanceData {
        String instanceKey;
        Channel instanceChannel;
        EventLoopGroup workerGroup;
    }

    private class ConnectionHandler implements WebSocketServerHandler.ConnectionHandler {
        private final Instance instance;

        ConnectionHandler(Instance instance) {
            this.instance = instance;
        }


        @Override
        public Optional<Player.Id> findOrCreatePlayer(String nick) {
            Optional<Player.Id> player = playerService.findPlayer(nick);
            if (player.isPresent()) {
                return player;
            }
            return playerService.createPlayer(nick).toOptional().map(Entity::getId);
        }

        @Override
        public boolean canPlayerConnect(Player.Id playerId) {
            return !playerService.isPlayerPlaying(playerId);
        }

        @Override
        public void handleConnection(Channel channel, Player.Id playerId) {
            players.put(channel.id(), playerId);
            playerService.loginPlayer(playerId);
            instance.addPlayer(channel, playerId);
        }

        @Override
        public void handleMessage(Channel channel, String request) {
            instance.parseMessage(channel, request);
        }

        @Override
        public void handleDisconnection(Channel channel) {
            ChannelId channelId = channel.id();
            Player.Id playerId = players.get(channelId);
            players.remove(channelId);
            playerService.logoutPlayer(playerId);
            instance.removePlayer(channel);
        }
    }
}
