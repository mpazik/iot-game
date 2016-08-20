package dzida.server.app.instance.scenario;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public interface ScenarioLogic {
    default void handlePlayerDead(Id<Character> characterId) {
    }

    default void handleNpcDead(Id<Character> characterId) {
    }

    void start();
}
