define((require) => {
    const uiState = require('../../store/ui-state');

    return {
        key: 'casting-bar',
        type: 'fragment',
        requirements: {
            casting: Predicates.isSet()
        },
        template: '<div class="progress-bar"></div>',
        attached(element) {
            const casting = uiState.casting.value;
            element.getElementsByClassName('progress-bar')[0].style.animationDuration = casting + 'ms';
        }
    };
});