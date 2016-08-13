package dzida.server.app;

import dzida.server.app.command.Command;
import dzida.server.app.network.ConnectionHandler;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.player.Player;

public interface InstanceConnectionHandler {

    void playerConnected(Id<Player> playerId, ConnectionHandler.ConnectionController connectionController);

    void playerDisconnected(Id<Player> playerId);

    void playerJoinedToInstance(Id<Player> playerId, Key<Instance> instanceKey);

    void handleCommand(Id<Player> playerId, Command command);
}
