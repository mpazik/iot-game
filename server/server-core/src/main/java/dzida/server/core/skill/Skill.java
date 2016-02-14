package dzida.server.core.skill;

import dzida.server.core.basic.entity.Id;

public class Skill {
    private final Id<Skill> id;
    private final int type;
    private final double damage;
    private final double range;
    private final int cooldown;
    private final int target;
    private final int worldObject;

    public Skill(Id<Skill> id, int type, double damage, double range, int cooldown, int target, int worldObject) {
        this.id = id;
        this.type = type;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.target = target;
        this.worldObject = worldObject;
    }

    public Id<Skill> getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public double getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getTarget() {
        return target;
    }

    public int getWorldObject() {
        return worldObject;
    }
}
