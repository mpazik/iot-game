const Ids = require('../public/src/common/model/skills').Ids;
const Types = require('../public/src/common/model/skills').Types;
const Targets = require('../public/src/common/model/skills').Targets;


module.exports.skills = {
    [Ids.PUNCH]: {
        id: Ids.PUNCH,
        key: "punch",
        name: "Punch",
        icon: "fist",
        type: Types.ATTACK,
        damage: 5,
        range: 1,
        cooldown: 1000,
        target: Targets.ENEMIES
    },
    [Ids.BOW_SHOT]: {
        id: Ids.BOW_SHOT,
        key: "bow-shot",
        icon: "bow",
        type: Types.ATTACK,
        damage: 10,
        range: 5,
        projectile: 'arrow.png',
        cooldown: 2000,
        target: Targets.ENEMIES
    },
    [Ids.SWORD_HIT]: {
        id: Ids.SWORD_HIT,
        key: "sword-hit",
        name: "Sword Hit",
        icon: "stone_sword",
        type: Types.ATTACK,
        damage: 20,
        range: 1,
        cooldown: 1500,
        target: Targets.ENEMIES
    }
};