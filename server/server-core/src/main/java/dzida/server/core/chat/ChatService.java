package dzida.server.core.chat;

import com.google.common.collect.ImmutableList;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.event.GameEvent;
import dzida.server.core.player.Player;
import dzida.server.core.player.PlayerService;

import java.util.List;

public class ChatService {
    private final PlayerService playerService;

    public ChatService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public List<GameEvent> handleMessage(Id<Player> playerId, String message) {
        String nick = playerService.getPlayer(playerId).getData().getNick();
        return ImmutableList.of(new PlayerMessage(nick, message));
    }
}
