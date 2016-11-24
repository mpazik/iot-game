define((require) => {
    const Predicates = require('../../common/predicates');
    const uiState = require('../../store/ui-state');

    return {
        key: 'server-error',
        type: 'fragment',
        requirements: {
            serverError: Predicates.isSet()
        },
        template: '<span></span>',
        attached(element) {
            element.getElementsByTagName('span')[0].innerText = uiState.serverError.value;
        }
    };
});