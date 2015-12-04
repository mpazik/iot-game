define(function (require, exports, module) {

    function Player(id, nick, move) {
        this.id = id;
        this.nick = nick;
        this.move = move;
    }

    Player.fromObject = function (data) {
        return new Player(data.id, data.nick, data.move)
    };

    module.exports = Player;
});