define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    module.exports = new Publisher.StatePublisher(null, (push) => {
        Dispatcher.messageStream.subscribe(MessageIds.InitialData, (event) => {
            push(event.scenario)
        });
    })
});