define(function (require, exports, module) {
    const Application = require('./application');
    const Publisher = require('../common/basic/publisher');
    const Commands = require('../common/packet/commands').constructors;
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    module.exports = {
        sendMessage: function (message) {
            Application.sendCommands([new Commands.SendMessage(message)]);
        },
        playerMessage: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribe(MessageIds.PlayerMessage, (data) => {
                push(data)
            });
        })
    };
});