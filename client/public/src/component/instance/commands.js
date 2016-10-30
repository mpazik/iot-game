define(function (require, exports, module) {
    module.exports = {
        Move: function (x, y) {
            this.x = x;
            this.y = y;
        },
        UseSkillOnCharacter: function (skillId, target) {
            this.skillId = skillId;
            this.target = target;
        },
        BuildObject: function (objectKindId, x, y) {
            this.objectKindId = objectKindId;
            this.x = x;
            this.y = y;
        },
        Backdoor: function (type, data) {
            this.type = type;
            this.data = data;
        },
        UseSkillOnWorldObject: function (skillId, target) {
            this.skillId = skillId;
            this.target = target;
        },
        EatApple: function () {
        },
        EatRottenApple: function () {
        }
    };
});