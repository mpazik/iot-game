package dzida.server.app.instance.skill;

import dzida.server.app.basic.entity.Id;
import dzida.server.app.instance.character.event.CharacterDied;
import dzida.server.app.instance.character.event.CharacterSpawned;
import dzida.server.app.instance.character.model.Character;
import dzida.server.app.instance.event.GameEvent;
import dzida.server.app.instance.skill.event.CharacterGotDamage;
import dzida.server.app.instance.skill.event.CharacterHealed;
import dzida.server.app.instance.skill.event.SkillUsedOnCharacter;
import dzida.server.app.instance.skill.event.SkillUsedOnWorldMap;
import dzida.server.app.instance.skill.event.SkillUsedOnWorldObject;
import dzida.server.app.time.TimeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class SkillService {
    public static final String Key = "skill";

    private final Map<Id<Character>, SkillData> state = new HashMap<>();
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

    public boolean isOnCooldown(Id<Character> casterId, long time) {
        return time < state.get(casterId).cooldownTill;
    }

    public Skill getSkill(Id<Skill> skillId) {
        return skillsStore.getSkill(skillId);
    }

    public int getHealth(Id<Character> characterId) {
        return state.get(characterId).health;
    }

    public int getMaxHealth(Id<Character> characterId) {
        return state.get(characterId).maxHealth;
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
            skillData.health = skillData.health - (int) event.damage;
        })
                .is(CharacterHealed.class).then(event -> {
            SkillData skillData = state.get(event.characterId);
            skillData.health = skillData.health + (int) event.healed;
        });
    }

    private void setCharacterCooldown(Id<Character> casterId, Id<Skill> skillId) {
        int skillCooldown = getSkill(skillId).getCooldown();
        state.get(casterId).cooldownTill = timeService.getCurrentMillis() + skillCooldown;
    }

    public SkillData getInitialSkillData(int characterType) {
        if (characterType == Character.Type.Bot) {
            return new SkillData(20, 20, 0);
        }
        if (characterType == Character.Type.Player) {
            return new SkillData(300, 300, 0);
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
        Id<Character> characterId;
        SkillData skillData;

        public SkillCharacterState(Id<Character> characterId, SkillData skillData) {
            this.characterId = characterId;
            this.skillData = skillData;
        }
    }
}
