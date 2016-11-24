define(() => {
    return {
        key: 'connecting-screen',
        type: 'fragment',
        requirements: {
            applicationState: Predicates.is('connecting')
        },
        template: 'Connecting...',
        classes: ['game-state']
    }
});