package dzida.server.core.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlayerStoreInMemory implements PlayerStore {
    private long lastId = 0;
    private final Map<Player.Id, Player.Entity> players = new HashMap<>();

    @Override
    public Stream<Player.Entity> getAllPlayers() {
        return players.values().stream();
    }

    @Override
    public Player.Entity createPlayer(Player.Data data) {
        lastId += 1;
        Player.Id id = new Player.Id(lastId);
        Player.Entity entity = new Player.Entity(id, data);
        players.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Player.Id> findPlayerByNick(String nick) {
        Optional<Player.Entity> foundPlayer = players.values().stream().filter(entity -> entity.getData().getNick().equals(nick)).findFirst();
        return foundPlayer.map(Player.Entity::getId);
    }

    @Override
    public Player.Entity getPlayer(Player.Id playerId) {
        return players.get(playerId);
    }

    @Override
    public void updatePlayer(Player.Id id, Player.Data newData) {
        Player.Entity newEntity = new Player.Entity(id, newData);
        players.put(id, newEntity);
    }
}
