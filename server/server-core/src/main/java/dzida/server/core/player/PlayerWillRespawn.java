package dzida.server.core.player;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.event.GameEvent;

public class PlayerWillRespawn implements GameEvent {
    public final Id<Player> playerId;
    public final long respawnTime;

    public PlayerWillRespawn(Id<Player> playerId, long respawnTime) {
        this.playerId = playerId;
        this.respawnTime = respawnTime;
    }

    @Override
    public int getId() {
        return GameEvent.PlayerWillRespawn;
    }
}
