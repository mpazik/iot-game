package dzida.server.app.npc;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.CharacterService;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionCommandHandler;
import dzida.server.core.position.PositionService;
import dzida.server.core.position.model.Position;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.SkillCommandHandler;
import dzida.server.core.skill.SkillService;
import dzida.server.core.skill.Skills;
import dzida.server.core.time.TimeService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NpcBehaviour {
    private static int MoveRadius = 10;
    private static int AggroRange = 8;

    private final PositionService positionService;
    private final CharacterService characterService;
    private final SkillService skillService;
    private final TimeService timeService;
    private final SkillCommandHandler skillCommandHandler;
    private final PositionCommandHandler positionCommandHandler;

    public NpcBehaviour(
            PositionService positionService,
            CharacterService characterService,
            SkillService skillService,
            TimeService timeService,
            SkillCommandHandler skillCommandHandler,
            PositionCommandHandler positionCommandHandler
    ) {
        this.positionService = positionService;
        this.characterService = characterService;
        this.skillService = skillService;
        this.timeService = timeService;
        this.skillCommandHandler = skillCommandHandler;
        this.positionCommandHandler = positionCommandHandler;
    }

    public List<GameEvent> processGameEvent(NpcImpl npc, GameEvent gameEvent) {
        return Collections.emptyList();
    }

    public List<GameEvent> processTick(NpcImpl npc) {
        Stream<CharacterId> players = characterService.getCharactersOfType(PlayerCharacter.class).stream().map(Character::getId);
        NpcCharacter npcCharacter = npc.getCharacter();
        Optional<CharacterId> target = players.filter(player -> positionService.areCharactersInDistance(npcCharacter.getId(), player, AggroRange, timeService.getCurrentMillis())).findAny();
        if (target.isPresent()) {
            if (isInAttackRange(npcCharacter, target.get())) {
                return tryAttackPlayer(npcCharacter, target.get());
            } else {
                return gotToPlayer(npcCharacter, target.get());
            }
        } else {
            if (isStanding(npcCharacter)) {
                return gotToRandomPosition(npcCharacter.getId());
            }
        }
        return Collections.emptyList();
    }

    private List<GameEvent> gotToRandomPosition(CharacterId id) {
        Position pos = positionService.getPosition(id, timeService.getCurrentMillis());
        Position direction = Position.of(randomCord(pos.getX()), randomCord(pos.getY()));
        return positionCommandHandler.move(id, direction, PositionService.BotSpeed);
    }

    private double randomCord(double cord) {
        return cord + Math.random() * MoveRadius;
    }

    private boolean isStanding(NpcCharacter npc) {
        return positionService.isStanding(npc.getId(), timeService.getCurrentMillis());
    }

    private List<GameEvent> gotToPlayer(NpcCharacter npc, CharacterId targetId) {
        Position direction = positionService.getPosition(targetId, timeService.getCurrentMillis());
        return positionCommandHandler.move(npc.getId(), direction, PositionService.BotSpeed);
    }

    private List<GameEvent> tryAttackPlayer(NpcCharacter npc, CharacterId targetId) {
        if (!skillService.isOnCooldown(npc.getId(), timeService.getCurrentMillis())) {
            return skillCommandHandler.useSkill(npc.getId(), getBotAttackSkillId(npc.getBotType()), targetId);
        }
        return Collections.emptyList();
    }

    private boolean isInAttackRange(NpcCharacter npc, CharacterId target) {
        Id<Skill> skillId = getBotAttackSkillId(npc.getBotType());
        Skill skill = skillService.getSkill(skillId);
        return positionService.areCharactersInDistance(npc.getId(), target, skill.getRange(), timeService.getCurrentMillis());
    }

    private Id<Skill> getBotAttackSkillId(int npcType) {
        if (npcType == Npc.Fighter) return Skills.Ids.SWORD_HIT;
        if (npcType == Npc.Archer) return Skills.Ids.BOW_SHOT;
        throw new UnsupportedOperationException("");
    }
}
