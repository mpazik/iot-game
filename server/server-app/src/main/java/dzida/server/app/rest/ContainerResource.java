package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.Gate;
import dzida.server.core.basic.Result;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class ContainerResource extends AbstractResource {

    private final Gate gate;

    public ContainerResource(Gate gate) {
        this.gate = gate;
    }

    @Path("can-player-login/{nick}")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder, @PathParam("nick") String nick) {
        boolean isPlayerPlaying = gate.isPlayerPlaying(nick);
        Result result = isPlayerPlaying ? Result.error("Player is currently logged in") : Result.ok();
        sendResult(responder, result);
    }
}