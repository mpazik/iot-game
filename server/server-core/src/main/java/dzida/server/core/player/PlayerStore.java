package dzida.server.core.player;


import dzida.server.core.basic.entity.Id;

import java.util.Optional;
import java.util.stream.Stream;

public interface PlayerStore {
    Stream<Player> getAllPlayers();

    Player createPlayer(Player.Data playerData);

    Optional<Id<Player>> findPlayerByNick(String nick);

    Player getPlayer(Id<Player> playerId);

    void updatePlayer(Id<Player> playerId, Player.Data player);
}
