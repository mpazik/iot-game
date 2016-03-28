package dzida.server.core.abilities;

import com.nurkiewicz.typeof.TypeOf;
import dzida.server.core.abilities.change.DiedChange;
import dzida.server.core.abilities.change.GotDamageChange;
import dzida.server.core.abilities.change.SkillUsedChange;
import dzida.server.core.abilities.change.SpawnedChange;
import dzida.server.core.entity.Change;
import dzida.server.core.entity.StateBuilder;
import dzida.server.core.skill.Skill;
import dzida.server.core.skill.Skills;

import java.time.Instant;

public class AbilitiesBuilder extends StateBuilder<Abilities> {

    private AbilitiesBuilder() {
    }

    @Override
    public void applyChange(Change<Abilities> skillChange) {
        TypeOf.whenTypeOf(skillChange).is(SpawnedChange.class).then(spawnedChange -> {
            updateState(new Abilities(spawnedChange.maxHealth, spawnedChange.maxHealth, Instant.MIN));
        }).is(DiedChange.class).then(diedChange -> {
            updateState(getState().updateHealth(0));
        }).is(GotDamageChange.class).then(gotDamageChange -> {
            updateState(getState().updateHealth(getState().getHealth() - gotDamageChange.damage));
        }).is(SkillUsedChange.class).then(skillUsedChange -> {
            Skill skill = Skills.get(skillUsedChange.usedSkillId);
            updateState(getState().updateCooldown(skillUsedChange.when.plusMillis(skill.getCooldown())));
        });
    }
}
