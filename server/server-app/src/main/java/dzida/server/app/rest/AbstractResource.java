package dzida.server.app.rest;

import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.Result;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.Charset;

public abstract class AbstractResource extends AbstractHttpHandler {

    private final Gson serializer = new Gson();
    protected ImmutableMultimap<String, String> headers = ImmutableMultimap.of("Access-Control-Allow-Origin", "*");

    public <T> T parseJsonRequest(HttpRequest request, Class<T> requestClass) {
        String data = request.getContent().toString(Charset.defaultCharset());
        return serializer.fromJson(data, requestClass);
    }

    protected void send(HttpResponder responder, Result result) {
        result.consume(
                () -> responder.sendStatus(HttpResponseStatus.NO_CONTENT, headers),
                error -> sendObject(responder, HttpResponseStatus.BAD_REQUEST, error)
        );
    }

    protected <T> void send(HttpResponder responder, Outcome<T> outcome) {
        outcome.consume(
                data -> sendObject(responder, data),
                error -> sendObject(responder, HttpResponseStatus.BAD_REQUEST, error)
        );
    }

    protected void sendObject(HttpResponder responder, Object data) {
        sendObject(responder, HttpResponseStatus.OK, data);
    }

    protected void sendObject(HttpResponder responder, HttpResponseStatus status, Object data) {
        String json = serializer.toJson(data);
        sendJson(responder, status, json);
    }

    protected void sendJson(HttpResponder responder, HttpResponseStatus status, String json) {
        ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(Charsets.UTF_8.encode(json));
        responder.sendContent(status, channelBuffer, "application/json", headers);
    }
}
