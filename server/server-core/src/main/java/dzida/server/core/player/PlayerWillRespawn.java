package dzida.server.core.player;

import dzida.server.core.event.GameEvent;

public class PlayerWillRespawn implements GameEvent {
    public final Player.Id playerId;
    public final long respawnTime;

    public PlayerWillRespawn(Player.Id playerId, long respawnTime) {
        this.playerId = playerId;
        this.respawnTime = respawnTime;
    }

    @Override
    public int getId() {
        return GameEvent.PlayerWillRespawn;
    }
}
