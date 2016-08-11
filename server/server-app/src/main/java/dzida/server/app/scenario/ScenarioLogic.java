package dzida.server.app.scenario;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.player.Player;

public interface ScenarioLogic {
    default void handlePlayerDead(Id<Character> characterId, Id<Player> playerId) {
    }

    default void handleNpcDead(Id<Character> characterId) {
    }

    void start();

    default void playerJoined(PlayerCharacter character) {
    }
}
