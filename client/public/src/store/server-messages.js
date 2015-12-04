define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../common/packet/messages');
    const ServerMessage = Messages.constructors.ServerMessage;

    var publishMessage;
    const messageToShowState = new Publisher.StatePublisher(null, function (fn) {
        return publishMessage = fn;
    });

    const delay = 1000;

    function showMessage(message) {
        publishMessage(message);
        setTimeout(function () {
            publishMessage(null);
        }, delay);
    }

    Dispatcher.messageStream.subscribe(Messages.ids.ServerMessage, function (event) {
        if (event.type == ServerMessage.Kinds.INFO) {
            showMessage(event.message);
        } else if (event.type == ServerMessage.Kinds.ERROR) {
            console.error(event.message)
        }
    });

    module.exports = {
        messageToShowState: messageToShowState
    };
});