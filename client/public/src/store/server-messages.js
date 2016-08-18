define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../component/instnace/messages');
    const ServerMessage = Messages.ServerMessage;

    const delay = 1000;

    function showMessage(publishMessage, message) {
        publishMessage(message);
        setTimeout(function () {
            publishMessage(null);
        }, delay);
    }

    module.exports = {
        messageToShowState: new Publisher.StatePublisher(null, push => {
            Dispatcher.messageStream.subscribe(Messages.ServerMessage, function (event) {
                if (event.type == ServerMessage.Kinds.INFO) {
                    showMessage(push, event.message);
                } else if (event.type == ServerMessage.Kinds.ERROR) {
                    console.error(event.message)
                }
            });
    })
    };
});