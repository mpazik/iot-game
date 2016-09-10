package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.leaderboard.Leaderboard;
import dzida.server.app.leaderboard.Leaderboard.PlayerScore;
import dzida.server.core.basic.entity.Id;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("/leaderboard")
public class LeaderboardResource extends AbstractResource {
    private final Leaderboard leaderboard;

    public LeaderboardResource(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @GET
    public void getLeaderboard(HttpRequest request, HttpResponder responder) {
        leaderboard.update();
        List<PlayerScore> userScores = leaderboard.getListOfSurvivalRecords();
        sendObject(responder, userScores);
    }

    @Path("user/{userId}")
    @GET
    public void getPlayerResult(HttpRequest request, HttpResponder responder, @PathParam("userId") Integer userId) {
        leaderboard.update();
        PlayerScore playerScore = leaderboard.getPlayerScore(new Id<>(userId));
        sendObject(responder, playerScore);
    }
}
