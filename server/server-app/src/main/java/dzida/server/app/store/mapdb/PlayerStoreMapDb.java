package dzida.server.app.store.mapdb;

import com.google.gson.Gson;
import dzida.server.app.Serializer;
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

    private final Gson serializer = Serializer.getSerializer();
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
    public Stream<Player.Entity> getAllPlayers() {
        return players.entrySet().stream()
                .map(entry -> {
                    Player.Data data = serializer.fromJson(entry.getValue(), Player.Data.class);
                    Player.Id id = new Player.Id(entry.getKey());
                    return new Player.Entity(id, data);
                });
    }

    @Override
    public Player.Entity createPlayer(Player.Data playerData) {
        Long newId = createNewId();
        Player.Id id = new Player.Id(newId);
        Player.Entity entity = new Player.Entity(id, playerData);
        players.put(newId, serializer.toJson(playerData));
        playersByNick.put(playerData.getNick(), newId);
        return entity;
    }

    @Override
    public Optional<Player.Id> findPlayerByNick(String nick) {
        return Optional.ofNullable(playersByNick.get(nick)).map(Player.Id::new);
    }

    @Override
    public Player.Entity getPlayer(Player.Id playerId) {
        Player.Data data = serializer.fromJson(players.get(playerId.getValue()), Player.Data.class);
        return new Player.Entity(playerId, data);
    }

    @Override
    public void updatePlayer(Player.Id playerId, Player.Data player) {
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
