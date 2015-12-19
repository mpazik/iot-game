package dzida.server.core.character;

import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Move;

import java.util.List;

import static java.util.Collections.singletonList;

public class CharacterCommandHandler {
    private final PositionService positionService;

    public CharacterCommandHandler(PositionService positionService) {
        this.positionService = positionService;
    }

    public List<GameEvent> addCharacter(Character character) {
        Move initialMove = positionService.getInitialMove(character.getId());
        return singletonList(new CharacterSpawned(character, initialMove));
    }

    public List<GameEvent> removeCharacter(CharacterId characterId) {
        return singletonList(new CharacterDied(characterId));
    }
}
