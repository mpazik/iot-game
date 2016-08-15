package dzida.server.app;

import dzida.server.app.instance.Instance;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class Gate {
    private final Map<Id<Player>, Key<Instance>> playersInstances;
    private final Set<BiConsumer<Id<Player>, Key<Instance>>> instanceChangeSubscribers;
    private final Set<Id<Player>> playingPlayers;

    private final PlayerService playerService;
    private final Key<Instance> defaultInstance;

    public Gate(PlayerService playerService, Key<Instance> defaultInstance) {
        playersInstances = new HashMap<>();
        instanceChangeSubscribers = new HashSet<>();
        playingPlayers = new HashSet<>();

        this.playerService = playerService;
        this.defaultInstance = defaultInstance;
    }

    public Optional<Id<Player>> authenticate(String nick) {
        return findOrCreatePlayer(nick);
    }

    public boolean isPlayerPlaying(String nick) {
        Optional<Id<Player>> playerIdOpt = playerService.findPlayer(nick);
        return playerIdOpt.isPresent() && playingPlayers.contains(playerIdOpt.get());
    }

    public void logoutPlayer(Id<Player> playerId) {
        playingPlayers.remove(playerId);
    }

    public void loginPlayer(Id<Player> playerId) {
        playingPlayers.add(playerId);
        Key<Instance> instanceKey = playersInstances.getOrDefault(playerId, defaultInstance);
        movePlayerToInstance(playerId, instanceKey);
    }

    public void subscribePlayerJoinedToInstance(BiConsumer<Id<Player>, Key<Instance>> playersInstanceChangeConsumer) {
        instanceChangeSubscribers.add(playersInstanceChangeConsumer);
    }

    public void movePlayerToDefaultInstance(Id<Player> playerId) {
        movePlayerToInstance(playerId, defaultInstance);
    }

    public void movePlayerToInstance(Id<Player> playerId, Key<Instance> instanceKey) {
        playersInstances.put(playerId, instanceKey);
        notifySubscribers(playerId, instanceKey);

    }

    public void notifySubscribers(Id<Player> playerId, Key<Instance> instanceKey) {
        instanceChangeSubscribers.forEach(idIdBiConsumer -> idIdBiConsumer.accept(playerId, instanceKey));
    }

    public Key<Instance> playerInstance(Id<Player> playerId) {
        return playersInstances.get(playerId);
    }

    private Optional<Id<Player>> findOrCreatePlayer(String nick) {
        Optional<Id<Player>> playerIdOpt = playerService.findPlayer(nick);
        if (playerIdOpt.isPresent()) {
            return playerIdOpt.filter(playerId -> !playingPlayers.contains(playerId));
        }
        Outcome<Player> player = playerService.createPlayer(nick);
        return player.toOptional().map(Player::getId);
    }
}
