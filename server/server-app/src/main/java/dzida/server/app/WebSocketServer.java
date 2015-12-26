package dzida.server.app;

import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import dzida.server.app.rest.ContainerResource;
import dzida.server.core.player.PlayerId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public final class WebSocketServer {

    public static void main(String[] args) throws IOException {
        if (Configuration.isDevMode()) {
            System.out.println("Server runs in DEV MODE!");
        }
        int startPort = Configuration.getFirstInstancePort();
        Container container = new Container(startPort, Configuration.getContainerWsAddress());

        for (String instance : Configuration.getInitialInstances()) {
            container.startInstance(instance, instance, (port) -> {
            }, null);
        }

        NettyHttpService service = NettyHttpService.builder()
                .setPort(Configuration.getContainerRestPort())
                .addHttpHandlers(ImmutableList.of(new ContainerResource(container)))
                .build();

        service.startAsync();
        service.awaitTerminated();

        container.shutdownGracefully();
    }
}

class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private final ConnectionHandler connectionHandler;
    private WebSocketServerHandshaker handshaker;

    public WebSocketServerHandler(ConnectionHandler connectionHandler) {
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
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        connectionHandler.handleDisconnection(ctx.channel());
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
            Optional<String> nick = readPlayerNickFromCookie(req);
            Optional<PlayerId> validPlayer = connectionHandler.isValidPlayer(nick.orElse(null));
            if (validPlayer.isPresent()) {
                ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);
                connectionHandler.handleConnection(channelFuture.channel(), validPlayer.get());
                System.out.println(String.format("Player %s connected", nick));
            } else {
                handshaker.close(ctx.channel(), new CloseWebSocketFrame(401, "Not valid nick"));
            }

        }
    }

    private Optional<String> readPlayerNickFromCookie(HttpRequest req) {
        Set<Cookie> cookies = ServerCookieDecoder.decode(req.headers().get("Cookie").toString());
        return cookies.stream().filter(cookie -> cookie.name().equals("nick")).findFirst().map(Cookie::value);
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
        connectionHandler.handleMessage(ctx.channel(), request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public interface ConnectionHandler {
        Optional<PlayerId> isValidPlayer(String nick);

        void handleConnection(Channel channel, PlayerId playerId);

        void handleMessage(Channel channel, String request);

        void handleDisconnection(Channel channel);
    }
}
