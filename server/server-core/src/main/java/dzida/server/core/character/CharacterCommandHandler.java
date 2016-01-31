package dzida.server.core.character;

import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.skill.SkillService;

import java.util.List;

import static java.util.Collections.singletonList;

public class CharacterCommandHandler {
    private final PositionService positionService;
    private final SkillService skillService;

    public CharacterCommandHandler(PositionService positionService, SkillService skillService) {
        this.positionService = positionService;
        this.skillService = skillService;
    }

    public List<GameEvent> spawnCharacter(Character character) {
        SkillService.SkillData initialSkillData = skillService.getInitialSkillData(character.getType());
        Move initialMove = positionService.getInitialMove(character.getId());
        return singletonList(new CharacterSpawned(character, initialMove, initialSkillData));
    }

    public List<GameEvent> killCharacter(CharacterId characterId) {
        return singletonList(new CharacterDied(characterId));
    }
}
