define(function (require) {
    const uiState = require('../../store/ui-state');

    return createUiElement('casting-bar', {
        type: 'fragment',
        properties: {
            requirements: {
                casting: Predicates.isSet()
            }
        },
        created: function () {
            this.innerHTML = '<div class="progress-bar"></div>';
        },
        attached: function () {
            const casting = uiState.casting.value;
            this.getElementsByClassName('progress-bar')[0].style.animationDuration = casting + 'ms';
        }
    });
});