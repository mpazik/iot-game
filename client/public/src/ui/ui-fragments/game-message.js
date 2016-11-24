define((require) => {
    const Predicates = require('../../common/predicates');
    const uiState = require('../../store/ui-state');

    return {
        key: 'game-message',
        type: 'fragment',
        requirements: {
            gameMessage: Predicates.isSet()
        },
        template: '<span></span>',
        attached(element) {
            element.getElementsByTagName('span')[0].innerText = uiState.gameMessage.value;
        }
    };
});