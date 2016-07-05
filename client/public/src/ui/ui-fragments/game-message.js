define(function (require, exports, module) {
    const uiState = require('../../store/ui-state');
    
    return createUiElement('game-message', {
        type: 'fragment',
        properties: {
            requirements: {
                gameMessage: Predicates.isSet()
            }
        },
        created: function () {
            this.innerHTML = '<span></span>';
        },
        attached: function () {
            this.getElementsByTagName('span')[0].innerText = uiState.gameMessage.value;
        }
    });
});