define(function (require, exports, module) {
    const Application = require('./application');
    const BackdoorCommands = require('./instnace/backdoor-commands');
        module.exports = {
        killCharacter: function () {
            Application.sendCommand(BackdoorCommands.killCharacter());
        }
    };
});