define(function (require, exports, module) {
    const backdoor = require('../../src/component/backdoor');
    
    return createUiElement('debug-window', {
        type: 'window',
        properties: {
            activateKeyBind: KEY_CODES.fromLetter("D"),
            keyBinds: [
                [KEY_CODES.fromLetter("K"), "killCharacter"]
            ],
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },
        created: function () {
            this.innerHTML = `<button id="kill-character"><b>K</b> - kill character</button>`;
        },
        killCharacter: function () {
            backdoor.killCharacter();
        },
        attached: function () {
            document.getElementById("kill-character").addEventListener("click", this.killCharacter.bind(this));
        }
    });
});