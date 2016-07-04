define(function (require, exports, module) {
    return createUiElement('loading-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                applicationState: Predicates.is('loading-game-assets')
            }
        },
        created: function () {
            this.innerHTML = 'Loading...';
            this.classList.add('game-state')
        }
    });
});