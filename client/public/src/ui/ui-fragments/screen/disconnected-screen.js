define(function (require) {
    const app = require('../../../component/application');
    
    return createUiElement('disconnected-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                instanceState: Predicates.is('disconnected')
            }
        },
        created: function () {
            this.innerHTML = `
<p>Disconnected from the server.</p>
<p><button class="large" autofocus>Reconnect</button></p>`;
            this.classList.add('game-state')
        },
        attached: function () {
            const button = this.getElementsByTagName("button")[0];
            button.onclick = function () {
                app.connect();
            };
            deffer(function () {
                button.focus();
            });
        }
    });
});