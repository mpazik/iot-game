define(function (require, exports, module) {
    return createUiElement('cooldown-bar', {
        type: 'fragment',
        properties: {
            requirements: {
                cooldown: Predicates.isSet()
            }
        },
        created: function () {
            this.innerHTML = '<div class="progress-bar"></div>';
        },
        attached: function () {
            const cooldown = this.uiState.cooldown.value.cooldown;
            this.getElementsByClassName('progress-bar')[0].style.animationDuration = cooldown + 'ms';
        }
    });
});