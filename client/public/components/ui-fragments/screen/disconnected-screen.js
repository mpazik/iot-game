define(function (require, exports, module) {
    return createUiElement('disconnected-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                applicationState: Predicates.is('disconnected')
            }
        },
        created: function () {
            this.innerHTML = `
<p>Disconnected from the server.</p>
<p><button class="large" autofocus>Reconnect</button></p>`;
            this.classList.add('game-state')
        },
        attached: function () {
            const game = this.game;
            const button = this.getElementsByTagName("button")[0];
            button.onclick = function () {
                game.connect();
            };
            deffer(function () {
                button.focus();
            });
        }
    });
});