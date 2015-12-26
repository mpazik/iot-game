package dzida.server.core.player;

import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class PlayerWillRespawn implements GameEvent {

    Player.Id playerId;
    long respawnTime;

    @Override
    public int getId() {
        return GameEvent.PlayerWillRespawn;
    }
}
