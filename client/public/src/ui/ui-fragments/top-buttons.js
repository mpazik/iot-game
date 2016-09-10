define(function (require) {
    const userEventStream = require('../../component/dispatcher').userEventStream;
    
    return createUiElement('join-battle-button', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            this.innerHTML = `<div>
    <button id="join-battle-button"><span class="action-key-shortcut">J</span>oin Battle!</button>
    <button id="give-feedback-button"><span class="action-key-shortcut">G</span>ive feedback!</button>
</div>`;
        },
        attached: function () {
            document.getElementById('join-battle-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'join-battle-window');
            });
            document.getElementById('give-feedback-button').addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'feedback-window');
            });
        }
    });
});