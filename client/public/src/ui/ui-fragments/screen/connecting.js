define(function () {
    return createUiElement('connecting-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                applicationState: Predicates.is('connecting')
            }
        },
        created: function () {
            this.innerHTML = 'Connecting...';
            this.classList.add('game-state')
        }
    });
});