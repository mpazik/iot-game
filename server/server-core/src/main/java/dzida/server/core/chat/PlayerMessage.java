package dzida.server.core.chat;

import dzida.server.core.event.GameEvent;

public class PlayerMessage implements GameEvent {
    String playerNick;
    String message;

    public PlayerMessage(String playerNick, String message) {
        this.playerNick = playerNick;
        this.message = message;
    }

    @Override
    public int getId() {
        return GameEvent.PlayerMessage;
    }
}
