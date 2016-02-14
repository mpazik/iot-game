define(function (require, exports, module) {
    const commands = {
        constructors: {
            LogIn: function (nick) {
                this.nick = nick;
            },

            LogOut: function () {
            },
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
            PlayingPlayer: function (playerId) {
                this.playerId = playerId;
            },
            TimeSync: function (clientTime) {
                this.clientTime = clientTime;
            },
            JoinBattle: function (map, difficultyLevel) {
                this.map = map;
                this.difficultyLevel = difficultyLevel
            },
            Backdoor: function (type, data) {
                this.type = type;
                this.data = data;
            },
            GoToHome: function () {},
            SendMessage: function (message) {
                this.message = message;
            },
            UseSkillOnWorldObject: function (skillId, target) {
                this.skillId = skillId;
                this.target = target;
            }
        },
        ids: {
            LogIn: 0,
            LogOut: 1,
            Move: 2,
            UseSkillOnCharacter: 3,
            UseSkillOnWorldMap: 4,
            PlayingPlayer: 5,
            TimeSync: 6,
            JoinBattle: 7,
            Backdoor: 8,
            GoToHome: 9,
            SendMessage: 10,
            UseSkillOnWorldObject: 11
        },
        forId: []
    };

    require('./messages-functions').setIdToPrototype(commands);
    require('./messages-functions').createForIds(commands);
    module.exports = commands;
});