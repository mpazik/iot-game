define(() => {
    return {
        key: 'loading-screen',
        type: 'fragment',
        requirements: {
            instanceState: Predicates.is('loading-game-assets')
        },
        template: 'Loading...',
        classes: ['game-state']
    }
});