package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.Container;
import dzida.server.app.Serializer;
import dzida.server.core.basic.Result;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class ContainerResource extends AbstractResource {

    private final Container container;

    public ContainerResource(Serializer serializer, Container container) {
        super(serializer);
        this.container = container;
    }

    @Path("can-player-login/{nick}")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder, @PathParam("nick") String nick) {
        Result result = container.canPlayerLogIn(nick);
        sendResult(responder, result);
    }

}