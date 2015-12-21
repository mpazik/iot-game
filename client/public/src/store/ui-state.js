define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MainPlayerStore = require('./main-player');

    var setState;

    function updateProperty(property, value) {
        uiState[property] = value;
        setState(uiState);
    }

    function updatePropertyAction(property) {
        return function (value) {
            updateProperty(property, value);
        }
    }

    var uiState = {
        playerAlive: MainPlayerStore.playerLiveState.value
    };

    MainPlayerStore.playerLiveState.subscribe(updatePropertyAction('playerAlive'));

    module.exports = {
        state: new Publisher.StatePublisher(true, (push) => setState = push)
    };
});