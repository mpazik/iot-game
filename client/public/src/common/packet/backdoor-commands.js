define(function (require, exports, module) {
    const Commands = require("./commands").constructors;

    const CommandIds = {
        KillCharacter: 0
    };

    module.exports = {
        killCharacter: function () {
            return new Commands.Backdoor(CommandIds.KillCharacter, {})
        }
    };
});