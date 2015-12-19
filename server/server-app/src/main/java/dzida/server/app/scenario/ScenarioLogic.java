package dzida.server.app.scenario;

import dzida.server.core.character.CharacterId;
import dzida.server.core.player.PlayerId;

public interface ScenarioLogic {
    void handlePlayerDead(CharacterId characterId, PlayerId playerId);

    void start();
}
