define(function (require, exports, module) {

    const Types = {
        ATTACK: 0,
        BUILD: 1,
        GATHER: 2,
        CRAFT: 3,
        USE: 4,
        SPECIAL: 5
    };

    const Targets = {
        ENEMIES: 0,
        USERS: 1
    };

    const Ids = {
        PUNCH: 0,
        BOW_SHOT: 1,
        SWORD_HIT: 2,
        CREATE_TREE: 3,
        CUT_TREE: 4,
        CREATE_ARROWS: 5,
        INTRODUCE: 6,
        GRAB_APPLE: 7,
        EAT_APPLE: 8,
    };

    module.exports = {
        Types,
        Ids,
        Targets,
        keyToId(skillKey) {
            const skillIdName = skillKey.toUpperCase().replace('-', '_');
            const skillId = Ids[skillIdName];
            if (skillId == null) {
                throw `There is no skill id for a skill ${skillKey}`
            }
            return skillId
        }
    };
});