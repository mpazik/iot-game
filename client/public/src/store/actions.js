define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');

    var castingFinishTimeout = null;
    var pushCastingTime = null;

    function doActionOnObject(data) {
        Dispatcher.messageStream.publish('action-completed-on-world-object', data);
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

    function subscribeCanceling() {
        // current action is triggered by left click so we don't want to subscribe to current mouse click
        deffer(() => {
            Dispatcher.userEventStream.subscribe('right-click', cancelAction);
            Dispatcher.userEventStream.subscribe('left-click', cancelAction);
            Dispatcher.userEventStream.subscribe('esc-down', cancelAction);
        });
    }

    Dispatcher.messageStream.subscribe('action-started-on-world-object', (data) => {
        clearTimeout(castingFinishTimeout);
        if (data.action.key == 'cook') {
            Dispatcher.userEventStream.publish('toggle-window', 'cooking-window');
            return;
        }
        castingFinishTimeout = setTimeout(() => doActionOnObject(data), data.action.casting);
        pushCastingTime(data.action.casting);
        subscribeCanceling();
    });

    function doCookingAction(recipe) {
        Dispatcher.messageStream.publish('action-cooked', recipe);
        cancelAction();
    }

    Dispatcher.userEventStream.subscribe('action-started-cooking', (recipe) => {
        castingFinishTimeout = setTimeout(() => doCookingAction(recipe), recipe.cookingTime);
        pushCastingTime(recipe.cookingTime);
        subscribeCanceling();
    });

    module.exports = {
        playerCasting: new Publisher.StatePublisher(null, push => {
            pushCastingTime = push;
        })
    }
});
