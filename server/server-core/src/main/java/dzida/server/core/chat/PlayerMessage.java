package dzida.server.core.chat;

import dzida.server.core.event.GameEvent;
import lombok.Value;

@Value
public class PlayerMessage implements GameEvent {
    String playerNick;
    String message;

    @Override
    public int getId() {
        return GameEvent.PlayerMessage;
    }
}
