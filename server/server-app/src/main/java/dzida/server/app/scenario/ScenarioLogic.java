package dzida.server.app.scenario;

import dzida.server.core.character.CharacterId;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.Player;

public interface ScenarioLogic {
    default void handlePlayerDead(CharacterId characterId, Player.Id playerId) {
    }

    default void handleNpcDead(CharacterId characterId) {
    }

    void start();

    default void playerJoined(PlayerCharacter character) {
    }
}
