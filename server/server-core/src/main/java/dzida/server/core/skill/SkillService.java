package dzida.server.core.skill;

import dzida.server.core.CharacterId;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsed;
import dzida.server.core.time.TimeService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class SkillService {
    public static final String Key = "skill";

    private final Map<CharacterId, SkillData> state = new HashMap<>();
    private final Map<Integer, Skill> skills;

    private final TimeService timeService;


    public SkillService(Map<Integer, Skill> skills, TimeService timeService) {
        this.skills = skills;
        this.timeService = timeService;
    }


    public static SkillService create(Map<Integer, Skill> skills, TimeService timeService) {
        return new SkillService(skills, timeService);
    }

    public List<SkillCharacterState> getState() {
        return state.entrySet().stream().map(entry -> new SkillCharacterState(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public String getKey() {
        return Key;
    }

    public boolean isOnCooldown(CharacterId casterId, long time) {
        return time < state.get(casterId).getCooldownTill();
    }

    public Skill getSkill(int skillId) {
        return skills.get(skillId);
    }

    public int getHealth(CharacterId characterId) {
        return state.get(characterId).getHealth();
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            Character character = event.getCharacter();
            state.put(character.getId(), characterInitState(character.getType()));
        }).is(CharacterDied.class).then(
                event -> state.remove(event.getCharacterId())
        ).is(SkillUsed.class).then(event -> {
            int skillCooldown = skills.get(event.getSkillId()).getCooldown();
            state.get(event.getCasterId()).setCooldownTill(timeService.getCurrentMillis() + skillCooldown);
        }).is(CharacterGotDamage.class).then(event -> {
            SkillData skillData = state.get(event.getCharacterId());
            int remainHp = skillData.getHealth() - (int) event.getDamage();
            skillData.setHealth(remainHp);
        });
    }

    private SkillData characterInitState(int characterType) {
        if (characterType == Character.Bot) {
            return new SkillData(100, 100, 0);
        }
        if (characterType == Character.Player) {
            return new SkillData(10, 200, 0);
        }
        throw new IllegalStateException("If this is throw that means there is time to change implementation to use enums");
    }

    @Data
    @AllArgsConstructor
    public final static class SkillData {
        int health;
        int maxHealth;
        long cooldownTill;
    }

    @Data
    @AllArgsConstructor
    public final static class SkillCharacterState {
        CharacterId characterId;
        SkillData skillData;
    }
}
