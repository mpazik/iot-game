define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../component/instance/messages');

    module.exports = {
        data: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(Messages.InitialData, (event) => {
                push(event.scenario)
            });
            Dispatcher.messageStream.subscribe(Messages.Disconnected, () => {
                push(null)
            });
        }),
        endScenarioData: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(Messages.InitialData, () => {
                push(null)
            });
            Dispatcher.messageStream.subscribe(Messages.ScenarioEnd, (event) => {
                setTimeout(() => push(event), 1000);
            });
            Dispatcher.messageStream.subscribe(Messages.Disconnected, () => {
                push(null)
            });
        })
    }
});