define((require) => {
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return {
        key: 'feedback-button',
        type: 'fragment',
        requirements: {
            playerAlive: Predicates.is(true)
        },
        template: `<button id="give-feedback-button"><span class="action-key-shortcut">G</span>ive feedback</button>`,
        attached() {
            document.getElementById('give-feedback-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'feedback-window');
            });
        }
    };
});
