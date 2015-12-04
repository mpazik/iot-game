package dzida.server.core.skill;

public class Skill {
    private final int id;
    private final int type;
    private final double damage;
    private final double range;
    private final int cooldown;
    private final int target;

    public Skill(int id, int type, double damage, double range, int cooldown, int target) {
        this.id = id;
        this.type = type;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.target = target;
    }

    public int getId() {
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
}
