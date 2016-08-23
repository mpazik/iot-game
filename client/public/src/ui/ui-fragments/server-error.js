define(function (require) {
    const uiState = require('../../store/ui-state');

    return createUiElement('server-error', {
        type: 'fragment',
        properties: {
            requirements: {
                serverError: Predicates.isSet()
            }
        },
        created: function () {
            this.innerHTML = '<span></span>';
        },
        attached: function () {
            this.getElementsByTagName('span')[0].innerText = uiState.serverError.value;
        }
    });
});