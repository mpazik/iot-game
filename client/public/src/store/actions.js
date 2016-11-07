define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');

    var castingFinishTimeout = null;
    var pushCastingTime = null;

    function doAction(action) {
        Dispatcher.messageStream.publish('action-completed-on-world-object', action);
        cancelAction();
    }

    function cancelAction() {
        pushCastingTime(null);
        clearTimeout(castingFinishTimeout);
        castingFinishTimeout = null;
        Dispatcher.userEventStream.unsubscribe('right-click', cancelAction);
        Dispatcher.userEventStream.unsubscribe('left-click', cancelAction);
        Dispatcher.userEventStream.unsubscribe('esc-down', cancelAction);
    }

    Dispatcher.messageStream.subscribe('action-started-on-world-object', (action) => {
        clearTimeout(castingFinishTimeout);
        castingFinishTimeout = setTimeout(() => doAction(action), action.casting);
        pushCastingTime(action.casting);
        // current action is triggered by left click so we don't want to subscribe to current mouse click
        deffer(() => {
            Dispatcher.userEventStream.subscribe('right-click', cancelAction);
            Dispatcher.userEventStream.subscribe('left-click', cancelAction);
            Dispatcher.userEventStream.subscribe('esc-down', cancelAction);
        });
    });

    module.exports = {
        playerCasting: new Publisher.StatePublisher(null, push => {
            pushCastingTime = push;
        })
    }
});
