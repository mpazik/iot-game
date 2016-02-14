package dzida.server.core.skill;

import dzida.server.core.basic.entity.Id;
import dzida.server.core.character.CharacterId;
import dzida.server.core.character.event.CharacterDied;
import dzida.server.core.character.event.CharacterSpawned;
import dzida.server.core.character.model.Character;
import dzida.server.core.event.GameEvent;
import dzida.server.core.skill.event.CharacterGotDamage;
import dzida.server.core.skill.event.SkillUsedOnCharacter;
import dzida.server.core.skill.event.SkillUsedOnWorldMap;
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
    private final SkillStore skillsStore;

    private final TimeService timeService;


    private SkillService(SkillStore skillsStore, TimeService timeService) {
        this.skillsStore = skillsStore;
        this.timeService = timeService;
    }


    public static SkillService create(SkillStore skillsStore, TimeService timeService) {
        return new SkillService(skillsStore, timeService);
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

    public Skill getSkill(Id<Skill> skillId) {
        return skillsStore.getSkill(skillId);
    }

    public int getHealth(CharacterId characterId) {
        return state.get(characterId).getHealth();
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent).is(CharacterSpawned.class).then(event -> {
            Character character = event.getCharacter();
            state.put(character.getId(), event.getSkillData());
        }).is(CharacterDied.class).then(
                event -> state.remove(event.getCharacterId())
        ).is(SkillUsedOnCharacter.class).then(event -> {
            int skillCooldown = getSkill(event.getSkillId()).getCooldown();
            state.get(event.getCasterId()).setCooldownTill(timeService.getCurrentMillis() + skillCooldown);
        }).is(SkillUsedOnWorldMap.class).then(event -> {
            int skillCooldown = getSkill(event.getSkillId()).getCooldown();
            state.get(event.getCasterId()).setCooldownTill(timeService.getCurrentMillis() + skillCooldown);
        }).is(CharacterGotDamage.class).then(event -> {
            SkillData skillData = state.get(event.getCharacterId());
            int remainHp = skillData.getHealth() - (int) event.getDamage();
            skillData.setHealth(remainHp);
        });
    }

    public SkillData getInitialSkillData(int characterType) {
        if (characterType == Character.Type.Bot) {
            return new SkillData(10, 50, 0);
        }
        if (characterType == Character.Type.Player) {
            return new SkillData(200, 200, 0);
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
