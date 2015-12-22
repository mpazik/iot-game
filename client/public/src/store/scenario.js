define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    module.exports = {
        data: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.InitialData, (event) => {
                push(event.scenario)
            });
        }),
        endScenarioData: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.InitialData, () => {
                push(null)
            });
            Dispatcher.messageStream.subscribe(MessageIds.ScenarioEnd, (event) => {
                push(event)
            });
        })
    }
});