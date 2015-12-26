package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.Leaderboard;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

public class LeaderboardResource extends AbstractResource {
    private final Leaderboard leaderboard;

    public LeaderboardResource(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Path("leaderboard")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder) {
        List<Leaderboard.Record> records = leaderboard.listOfSurvivalRecords();
        sendObject(responder, records);
    }
}
