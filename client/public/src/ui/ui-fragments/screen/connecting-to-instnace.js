define((require) => {
    const Predicates = require('../../../common/predicates');
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