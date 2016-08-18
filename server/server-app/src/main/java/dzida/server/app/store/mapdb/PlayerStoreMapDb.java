package dzida.server.app.store.mapdb;

import com.google.gson.Gson;
import dzida.server.app.BasicJsonSerializer;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerStore;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Stream;

public class PlayerStoreMapDb implements PlayerStore {

    private final Gson serializer = BasicJsonSerializer.getSerializer();
    private final ConcurrentNavigableMap<Long, String> players;
    private final ConcurrentNavigableMap<String, Long> playersByNick;

    public PlayerStoreMapDb() {
        DB db = DBMaker.fileDB(new File("playersDB"))
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();
        players = db.treeMap("players", BTreeKeySerializer.LONG, org.mapdb.Serializer.STRING);
        playersByNick = db.treeMap("playersByNick", BTreeKeySerializer.STRING, org.mapdb.Serializer.LONG);
    }

    @Override
    public Stream<Player> getAllPlayers() {
        return players.entrySet().stream()
                .map(entry -> {
                    Player.Data data = serializer.fromJson(entry.getValue(), Player.Data.class);
                    Id<Player> id = new Id<>(entry.getKey());
                    return new Player(id, data);
                });
    }

    @Override
    public Player createPlayer(Player.Data playerData) {
        Long newId = createNewId();
        Id<Player> id = new Id<>(newId);
        Player entity = new Player(id, playerData);
        players.put(newId, serializer.toJson(playerData));
        playersByNick.put(playerData.getNick(), newId);
        return entity;
    }

    @Override
    public Optional<Id<Player>> findPlayerByNick(String nick) {
        return Optional.ofNullable(playersByNick.get(nick)).map(Id<Player>::new);
    }

    @Override
    public Player getPlayer(Id<Player> playerId) {
        Player.Data data = serializer.fromJson(players.get(playerId.getValue()), Player.Data.class);
        return new Player(playerId, data);
    }

    @Override
    public void updatePlayer(Id<Player> playerId, Player.Data player) {
        players.put(playerId.getValue(), serializer.toJson(player));
    }

    private Long createNewId() {
        NavigableSet<Long> keySet = players.keySet();
        if (keySet.isEmpty()) {
            return 1L;
        }
        return keySet.last() + 1;
    }
}
