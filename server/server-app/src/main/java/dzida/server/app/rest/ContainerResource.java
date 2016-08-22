package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.arbiter.Arbiter;
import dzida.server.core.basic.Result;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class ContainerResource extends AbstractResource {

    private final Arbiter arbiter;

    public ContainerResource(Arbiter arbiter) {
        this.arbiter = arbiter;
    }

    @Path("can-player-login/{nick}")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder, @PathParam("nick") String nick) {
        boolean isPlayerPlaying = arbiter.isPlayerPlaying(nick);
        Result result = isPlayerPlaying ? Result.error("Player is currently logged in") : Result.ok();
        send(responder, result);
    }
}