define(function (require) {
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('feedback-button', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created: function () {
            this.innerHTML = `<button id="give-feedback-button"><span class="action-key-shortcut">G</span>ive feedback</button>`;
        },
        attached: function () {
            document.getElementById('give-feedback-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'feedback-window');
            });
        }
    });
});
