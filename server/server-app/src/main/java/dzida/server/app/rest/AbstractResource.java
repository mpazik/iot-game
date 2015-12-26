package dzida.server.app.rest;

import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import dzida.server.app.Serializer;
import dzida.server.core.basic.Result;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class AbstractResource extends AbstractHttpHandler {

    private final Gson serializer = Serializer.getSerializer();

    protected void sendResult(HttpResponder responder, Result result) {
        result.consume(validResult -> {
            responder.sendStatus(HttpResponseStatus.NO_CONTENT);
        }, errorResult -> {
            sendJson(responder, HttpResponseStatus.BAD_REQUEST, serializer.toJson(errorResult));
        });
    }

    private void sendJson(HttpResponder responder, HttpResponseStatus status, String json) {
        ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(Charsets.UTF_8.encode(json));
        responder.sendContent(status, channelBuffer, "application/json", ImmutableMultimap.<String, String>of());
    }
}
