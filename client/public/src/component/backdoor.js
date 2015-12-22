define(function (require, exports, module) {
    const Application = require('./application');
    const BackdoorCommands = require('../common/packet/backdoor-commands');
        module.exports = {
        killCharacter: function () {
            Application.sendCommands([BackdoorCommands.killCharacter()]);
        }
    };
});