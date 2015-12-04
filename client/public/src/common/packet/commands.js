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
            Ping: function (clientTime) {
                this.clientTime = clientTime;
            },
            PlayingPlayer: function (playerId) {
                this.playerId = playerId;
            },
            TimeSync: function (clientTime) {
                this.clientTime = clientTime;
            },
            JoinBattle: function (map) {
                this.map = map;
            }
        },
        ids: {
            LogIn: 0,
            LogOut: 1,
            Move: 2,
            UseSkill: 3,
            Ping: 4,
            PlayingPlayer: 5,
            TimeSync: 6,
            JoinBattle: 7
        },
        forId: []
    };

    require('./messages-functions').setIdToPrototype(commands);
    require('./messages-functions').createForIds(commands);
    module.exports = commands;
});