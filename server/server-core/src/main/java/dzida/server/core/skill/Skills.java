package dzida.server.core.skill;

import dzida.server.core.basic.entity.Id;

import java.util.Map;

public class Skills {
    public static class Types {
        public final static int ATTACK = 0;
        public final static int BUILDING = 1;
        public final static int GATHER = 2;
    }

    public static class Target {
        public final static int ENEMIES = 0;
    }

    public static class Ids {
        public final static Id<Skill> PUNCH = new Id<>(0);
        public final static Id<Skill> BOW_SHOT = new Id<>(1);
        public final static Id<Skill> SWORD_HIT = new Id<>(2);
    }

    public static Map<Id<Skill>, Skill> skills;

    public static Skill get(Id<Skill> skillId) {
        return skills.get(skillId);
    }
}
