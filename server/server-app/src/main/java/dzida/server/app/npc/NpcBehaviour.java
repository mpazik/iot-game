package dzida.server.app.npc;

import dzida.server.app.InstanceStateManager;
import dzida.server.app.command.InstanceCommand;
import dzida.server.app.command.MoveCommand;
import dzida.server.app.command.SkillUseOnCharacterCommand;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.model.Character;
import dzida.server.core.character.model.NpcCharacter;
import dzida.server.core.character.model.PlayerCharacter;
import dzida.server.core.event.GameEvent;
import dzida.server.core.position.PositionService;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.Skills;
import dzida.server.core.time.TimeService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NpcBehaviour {
    public static int MoveRadius = 10;
    public static int AggroRange = 8;

    private final TimeService timeService;
    private final InstanceStateManager state;

    public NpcBehaviour(TimeService timeService, InstanceStateManager state) {
        this.timeService = timeService;
        this.state = state;
    }

    public List<InstanceCommand> processTick(NpcImpl npc) {
        Stream<Id<Character>> players = state.getCharacterService().getCharactersOfType(PlayerCharacter.class).stream().map(Character::getId);
        NpcCharacter npcCharacter = npc.getCharacter();
        Optional<Id<Character>> target = players.filter(player -> state.getPositionService().areCharactersInDistance(npcCharacter.getId(), player, AggroRange, timeService.getCurrentMillis())).findAny();
        if (target.isPresent()) {
            if (isInAttackRange(npcCharacter, target.get())) {
                return tryAttackPlayer(npcCharacter, target.get());
            } else {
                return Collections.singletonList(gotToPlayer(npcCharacter, target.get()));
            }
        } else {
            if (isStanding(npcCharacter)) {
                return Collections.singletonList(gotToRandomPosition(npcCharacter.getId()));
            }
        }
        return Collections.emptyList();
    }

    private InstanceCommand gotToRandomPosition(Id<Character> id) {
        Point pos = state.getPositionService().getPosition(id, timeService.getCurrentMillis());
        Point direction = Point.of(randomCord(pos.getX()), randomCord(pos.getY()));
        return new MoveCommand(id, direction.getX(), direction.getY(), PositionService.BotSpeed);
    }

    private double randomCord(double cord) {
        return cord + Math.random() * MoveRadius;
    }

    private boolean isStanding(NpcCharacter npc) {
        return state.getPositionService().isStanding(npc.getId(), timeService.getCurrentMillis());
    }

    private InstanceCommand gotToPlayer(NpcCharacter npc, Id<Character> targetId) {
        Point direction = state.getPositionService().getPosition(targetId, timeService.getCurrentMillis());
        return new MoveCommand(npc.getId(), direction.getX(), direction.getY(), PositionService.BotSpeed);
    }

    private List<InstanceCommand> tryAttackPlayer(NpcCharacter npc, Id<Character> targetId) {
        if (!state.getSkillService().isOnCooldown(npc.getId(), timeService.getCurrentMillis())) {
            Id<Skill> skillId = getBotAttackSkillId(npc.getBotType());
            return Collections.singletonList(new SkillUseOnCharacterCommand(npc.getId(), skillId, targetId));
        }
        return Collections.emptyList();
    }

    private boolean isInAttackRange(NpcCharacter npc, Id<Character> target) {
        Id<Skill> skillId = getBotAttackSkillId(npc.getBotType());
        Skill skill = state.getSkillService().getSkill(skillId);
        return state.getPositionService().areCharactersInDistance(npc.getId(), target, skill.getRange(), timeService.getCurrentMillis());
    }

    private Id<Skill> getBotAttackSkillId(int npcType) {
        if (npcType == Npc.Fighter) return Skills.Ids.PUNCH;
        if (npcType == Npc.Archer) return Skills.Ids.BOW_SHOT;
        throw new UnsupportedOperationException("");
    }

    public List<InstanceCommand> processGameEvent(NpcImpl npc, GameEvent gameEvent) {
        return Collections.emptyList();
    }
}
