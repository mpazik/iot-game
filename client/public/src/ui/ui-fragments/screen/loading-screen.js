define(function () {
    return createUiElement('loading-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                instanceState: Predicates.is('loading-game-assets')
            }
        },
        created: function () {
            this.innerHTML = 'Loading...';
            this.classList.add('game-state')
        }
    });
});