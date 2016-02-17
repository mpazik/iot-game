if (typeof exports === 'object' && typeof exports.nodeName !== 'string' && typeof define !== 'function') {
    var define = function (factory) {
        factory(require, exports, module);
    };
}
define(function (require, exports, module) {

    const Types = {
        ATTACK: 0,
        BUILD: 1,
        GATHER: 2,
        CRAFT: 3
    };

    const Targets = {
        ENEMIES: 0
    };

    const Ids = {
        PUNCH: 0,
        BOW_SHOT: 1,
        SWORD_HIT: 2,
        CREATE_TREE: 3,
        CUT_TREE: 4,
        CREATE_ARROWS: 5
    };

    module.exports = {
        Types,
        Ids,
        Targets
    };
});