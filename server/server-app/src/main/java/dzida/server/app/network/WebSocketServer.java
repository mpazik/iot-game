package dzida.server.app.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.util.Random;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServer {
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup bossGroup;
    private final EventLoop eventLoop;

    public WebSocketServer() {
        workerGroup = new NioEventLoopGroup(1);
        bossGroup = new NioEventLoopGroup();
        eventLoop = workerGroup.next();
    }

    public void start(int port, ConnectionHandler connectionHandler) {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            pipeline.addLast("decoder", new HttpRequestDecoder());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                            //noinspection unchecked
                            pipeline.addLast("handler", new WebSocketHandler(connectionHandler));
                        }
                    });

            b.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public EventLoop getEventLoop() {
        return eventLoop;
    }

    public void shootDown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private static class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

        private static final String WEBSOCKET_PATH = "/websocket";
        private final ConnectionHandler connectionHandler;
        private WebSocketServerHandshaker handshaker;
        private int connectionId = 0;

        public WebSocketHandler(ConnectionHandler connectionHandler) {
            this.connectionHandler = connectionHandler;
        }

        private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
            // Generate an error page if response getStatus code is not OK (200).
            if (res.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
                HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
            }

            // Send the response and close the connection if necessary.
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private static String getWebSocketLocation(FullHttpRequest req) {
            return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
        }

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof FullHttpRequest) {
                if (connectionId != 0) {
                    fail(ctx, "Can not handle http request if connection is already established");
                    return;
                }
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                if (connectionId == 0) {
                    fail(ctx, "Can not handle websocket packet if connection is not already established");
                    return;
                }
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            if (connectionId != 0) {
                connectionHandler.handleDisconnection(connectionId);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            // Handle a bad request.
            if (!req.decoderResult().isSuccess()) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }

            // Allow only GET methods.
            if (req.method() != HttpMethod.GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }

            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                connectionId = new Random().nextInt();
                Channel channel = handshaker.handshake(ctx.channel(), req).channel();
                connectionHandler.handleConnection(connectionId, new ChannelConnect(channel));
                System.out.println(String.format("Received new connection: <[%s]>", connectionId));
            }
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

            // Check for closing frame
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                        .getName()));
            }

            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            connectionHandler.handleMessage(connectionId, request);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private void fail(ChannelHandlerContext ctx, String message) {
            System.err.println(message);
            ctx.close();
        }

        private final static class ChannelConnect implements Connection {
            private final Channel channel;

            private ChannelConnect(Channel channel) {
                this.channel = channel;
            }

            @Override
            public void disconnect() {
                channel.disconnect();
            }

            @Override
            public void send(String data) {
                channel.writeAndFlush(new TextWebSocketFrame(data));
            }
        }
    }

}