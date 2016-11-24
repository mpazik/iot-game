define(() => {
    return {
        key: 'connecting-to-instance-screen',
        type: 'fragment',
        requirements: {
            instanceState: Predicates.is('connecting')
        },
        template: 'Connecting...',
        classes: ['game-state']
    }
});