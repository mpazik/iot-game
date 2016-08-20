define(function () {
    return createUiElement('connecting-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                instanceState: Predicates.is('connecting')
            }
        },
        created: function () {
            this.innerHTML = 'Connecting...';
            this.classList.add('game-state')
        }
    });
});