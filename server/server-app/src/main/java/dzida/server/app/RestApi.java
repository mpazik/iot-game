package dzida.server.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Path("/schemas")
public class RestApi {

    @GET
    @Path("/hi")
    public String dropHi() {
        return "hi";
    }
}
