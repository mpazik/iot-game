package dzida.server.core.player;

import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class PlayerWillRespawn implements GameEvent {

    PlayerId playerId;
    long respawnTime;

    @Override
    public int getId() {
        return GameEvent.PlayerWillRespawn;
    }
}
