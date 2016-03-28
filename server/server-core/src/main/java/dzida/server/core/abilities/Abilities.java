package dzida.server.core.abilities;

import dzida.server.core.entity.State;
import dzida.server.core.skill.Skill;

import java.time.Instant;

public class Abilities implements State<Abilities> {
    private final int health;
    private final int maxHealth;
    private final Instant cooldownTill;

    public Abilities(int health, int maxHealth, Instant cooldownTill) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.cooldownTill = cooldownTill;
    }

    public boolean canUseSkill(Skill skill, Instant when) {
        return cooldownTill.isBefore(when);
    }

    public int getHealth() {
        return health;
    }

    public Abilities updateHealth(int newHealth) {
        return builder().setHealth(newHealth).build();
    }

    public Abilities updateCooldown(Instant cooldownTill) {
        return builder().setCooldownTill(cooldownTill).build();
    }

    private Builder builder() {
        return new Builder(health, maxHealth, cooldownTill);
    }

    private static final class Builder {
        private int health;
        private int maxHealth;
        private Instant cooldownTill;

        public Builder(int health, int maxHealth, Instant cooldownTill) {
            this.health = health;
            this.maxHealth = maxHealth;
            this.cooldownTill = cooldownTill;
        }

        public Builder setHealth(int health) {
            this.health = health;
            return this;
        }

        public Builder setMaxHealth(int maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public Builder setCooldownTill(Instant cooldownTill) {
            this.cooldownTill = cooldownTill;
            return this;
        }

        public Abilities build() {
            return new Abilities(health, maxHealth, cooldownTill);
        }
    }
}
