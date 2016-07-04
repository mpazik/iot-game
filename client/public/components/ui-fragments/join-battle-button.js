define(function (require, exports, module) {
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
            const element = this;
            this.getElementsByTagName("button")[0].onclick = function () {
                element.ui.toggleWindow("join-battle-window");
            };
        }
    });
});