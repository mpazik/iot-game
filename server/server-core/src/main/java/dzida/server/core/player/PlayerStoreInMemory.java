package dzida.server.core.player;

import dzida.server.core.basic.entity.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlayerStoreInMemory implements PlayerStore {
    private long lastId = 0;
    private final Map<Id<Player>, Player> players = new HashMap<>();

    @Override
    public Stream<Player> getAllPlayers() {
        return players.values().stream();
    }

    @Override
    public Player createPlayer(Player.Data data) {
        lastId += 1;
        Id<Player> id = new Id<>(lastId);
        Player entity = new Player(id, data);
        players.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Id<Player>> findPlayerByNick(String nick) {
        Optional<Player> foundPlayer = players.values().stream().filter(entity -> entity.getData().getNick().equals(nick)).findFirst();
        return foundPlayer.map(Player::getId);
    }

    @Override
    public Player getPlayer(Id<Player> playerId) {
        return players.get(playerId);
    }

    @Override
    public void updatePlayer(Id<Player> id, Player.Data newData) {
        Player newEntity = new Player(id, newData);
        players.put(id, newEntity);
    }
}
