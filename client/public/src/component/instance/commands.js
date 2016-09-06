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
        UseSkillOnWorldMap: function (skillId, x, y) {
            this.skillId = skillId;
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