package dzida.server.app.instance.scenario;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;

public class OpenWorldScenarioLogic implements ScenarioLogic {

    public OpenWorldScenarioLogic() {
    }

    @Override
    public void handlePlayerDead(Id<Character> characterId) {
        // Previously this class was responsible for respawn a player b but instance does not have access to players anymore.
        System.err.println("Character " + characterId + " shouldn't die on an open world scenario");
    }

    @Override
    public void start() {

    }

}
