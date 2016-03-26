package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.Leaderboard;
import dzida.server.app.Leaderboard.PlayerScore;
import dzida.server.app.Serializer;
import dzida.server.core.basic.Error;
import dzida.server.core.basic.Outcome;
import dzida.server.core.player.PlayerStore;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Optional;

@Path("/leaderboard")
public class LeaderboardResource extends AbstractResource {
    private final Leaderboard leaderboard;
    private final PlayerStore playerStore;

    public LeaderboardResource(Serializer serializer, Leaderboard leaderboard, PlayerStore playerStore) {
        super(serializer);
        this.leaderboard = leaderboard;
        this.playerStore = playerStore;
    }

    @GET
    public void getLeaderboard(HttpRequest request, HttpResponder responder) {
        List<PlayerScore> playerScores = leaderboard.getListOfSurvivalRecords();
        sendObject(responder, playerScores);
    }

    @Path("player/{playerNick}")
    @GET
    public void getPlayerResult(HttpRequest request, HttpResponder responder, @PathParam("playerNick") String playerNick) {
        Optional<PlayerScore> playerScore = playerStore.findPlayerByNick(playerNick).map(leaderboard::getPlayerScore);
        sendObject(responder, Outcome.fromOptional(playerScore, new Error("Player do not exists")));
    }
}
