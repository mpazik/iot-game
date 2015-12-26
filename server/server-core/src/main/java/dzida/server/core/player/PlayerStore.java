package dzida.server.core.player;


import java.util.Optional;

public interface PlayerStore {
    Player.Entity createPlayer(Player.Data playerData);

    Optional<Player.Id> findPlayerByNick(String nick);

    Player.Entity getPlayer(Player.Id playerId);

    void updatePlayer(Player.Id playerId, Player.Data player);
}
