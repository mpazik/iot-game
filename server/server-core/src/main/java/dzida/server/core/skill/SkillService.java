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
import dzida.server.core.skill.event.SkillUsedOnWorldObject;
import dzida.server.core.time.TimeService;

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
        return time < state.get(casterId).cooldownTill;
    }

    public Skill getSkill(Id<Skill> skillId) {
        return skillsStore.getSkill(skillId);
    }

    public int getHealth(CharacterId characterId) {
        return state.get(characterId).health;
    }

    public void processEvent(GameEvent gameEvent) {
        whenTypeOf(gameEvent)
                .is(CharacterSpawned.class).then(event -> {
            Character character = event.character;
            state.put(character.getId(), event.skillData);
        })
                .is(CharacterDied.class).then(event -> state.remove(event.characterId))
                .is(SkillUsedOnCharacter.class).then(event -> setCharacterCooldown(event.casterId, event.skillId))
                .is(SkillUsedOnWorldMap.class).then(event -> setCharacterCooldown(event.casterId, event.skillId))
                .is(SkillUsedOnWorldObject.class).then(event -> setCharacterCooldown(event.casterId, event.skillId))
                .is(CharacterGotDamage.class).then(event -> {
            SkillData skillData = state.get(event.characterId);
            int remainHp = skillData.health - (int) event.damage;
            skillData.health = remainHp;
        });
    }

    private void setCharacterCooldown(CharacterId casterId, Id<Skill> skillId) {
        int skillCooldown = getSkill(skillId).getCooldown();
        state.get(casterId).cooldownTill = timeService.getCurrentMillis() + skillCooldown;
    }

    public SkillData getInitialSkillData(int characterType) {
        if (characterType == Character.Type.Bot) {
            return new SkillData(30, 30, 0);
        }
        if (characterType == Character.Type.Player) {
            return new SkillData(200, 200, 0);
        }
        throw new IllegalStateException("If this is throw that means there is time to change implementation to use enums");
    }

    public final static class SkillData {
        int health;
        int maxHealth;
        long cooldownTill;

        public SkillData(int health, int maxHealth, long cooldownTill) {
            this.health = health;
            this.maxHealth = maxHealth;
            this.cooldownTill = cooldownTill;
        }
    }

    public final static class SkillCharacterState {
        CharacterId characterId;
        SkillData skillData;

        public SkillCharacterState(CharacterId characterId, SkillData skillData) {
            this.characterId = characterId;
            this.skillData = skillData;
        }
    }
}
