package dzida.server.core.character;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.basic.unit.Move;
import dzida.server.core.skill.SkillService;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class CharacterCommandHandler {
    private final PositionService positionService;
    private final SkillService skillService;
    private final CharacterService characterService;

    public CharacterCommandHandler(PositionService positionService, SkillService skillService, CharacterService characterService) {
        this.positionService = positionService;
        this.skillService = skillService;
        this.characterService = characterService;
    }

    public List<GameEvent> spawnCharacter(Character character, Point spawnPoint) {
        SkillService.SkillData initialSkillData = skillService.getInitialSkillData(character.getType());
        Move initialMove = positionService.getInitialMove(character.getId(), spawnPoint);
        return singletonList(new CharacterSpawned(character, initialMove, initialSkillData));
    }

    public List<GameEvent> killCharacter(Id<Character> characterId) {
        if (!characterService.isCharacterLive(characterId)) {
            return Collections.emptyList();
        }
        return singletonList(new CharacterDied(characterId));
    }
}
