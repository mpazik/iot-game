define(function (require) {
    const app = require('../../component/application');
    
    return createUiElement('settings-window', {
        type: 'window',
        properties: {
            keyBinds: [
                [KEY_CODES.fromLetter("L"), "logout"]
            ],
            requirements: {
                applicationState: Predicates.is('connected')
            }
        },
        created: function () {
            this.innerHTML = `<button class="logout"><span class="action-key-shortcut">L</span>ogout!</button>`;
        },
        attached: function () {
            this.getElementsByClassName("logout")[0].onclick = function () {
                app.logout();
            };
        },
        logout: function () {
            this.getElementsByClassName("logout")[0].click();
        }
    });
});