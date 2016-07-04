define(function (require, exports, module) {
    createUiElement('game-message', {
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
            this.getElementsByTagName('span')[0].innerText = this.uiState.gameMessage.value;
        }
    });
});