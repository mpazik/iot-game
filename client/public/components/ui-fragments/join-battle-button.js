define(function (require, exports, module) {
    const userEventStream = require('src/component/dispatcher').userEventStream;
    
    return createUiElement('join-battle-button', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            this.innerHTML = '<button><span class="action-key-shortcut">J</span>oin Battle!</button>';
        },
        attached: function () {
            this.getElementsByTagName('button')[0].onclick = function () {
                userEventStream.publish('toggle-window', 'join-battle-window');
            };
        }
    });
});