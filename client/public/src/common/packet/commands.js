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
            UseSkill: function (skillId, target) {
                this.skillId = skillId;
                this.target = target;
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
            }
        },
        ids: {
            LogIn: 0,
            LogOut: 1,
            Move: 2,
            UseSkill: 3,
            PlayingPlayer: 5,
            TimeSync: 6,
            JoinBattle: 7,
            Backdoor: 8,
            GoToHome: 9,
            SendMessage: 10
        },
        forId: []
    };

    require('./messages-functions').setIdToPrototype(commands);
    require('./messages-functions').createForIds(commands);
    module.exports = commands;
});