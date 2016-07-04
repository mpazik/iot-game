define(function (require, exports, module) {
    const App = require('src/component/application');
    const Dispatcher = require('src/component/dispatcher');
    const Backdoor = require('src/component/backdoor');
    const UiStateStore = require('src/store/ui-state');
    const Timer = require('src/component/timer');
    const Resources = require('src/store/resources');
    const Chat = require('src/component/chat');

    const gameTag = Object.create(HTMLElement.prototype, {
        createdCallback: {
            value: function () {
                this.innerHTML = `<div id="game-render"></div>`;
            }
        },
        attachedCallback: {
            value: function () {
                const gameRender = document.getElementById('game-render');
                App.setAddress(this.getAttribute("url"));
                App.init(gameRender);

                window.addEventListener('mousedown', function (event) {
                    if (event.which && event.which == 1) {
                        Dispatcher.userEventStream.publish('left-click', event);
                    }
                    if (event.which && event.which == 3) {
                        Dispatcher.userEventStream.publish('right-click', event);
                    }
                });

                window.addEventListener('keydown', function (event) {
                    if (event.keyCode == KEY_CODES.ESC) {
                        Dispatcher.userEventStream.publish('esc-down', event);
                    }
                });

                // because element is created asynchronously due to use requireJs we need to emit event when it's has been propertly created.
                this.dispatchEvent(new CustomEvent('element-attached'))
            }
        },
        logout: {value: App.logout},
        setUser: {value: App.setUser},
        connect: {value: App.connect},
        uiState: {value: UiStateStore},
        publishUiAction: {value: Dispatcher.userEventStream.publish.bind(Dispatcher.userEventStream)},
        backdoor: {value: Backdoor},
        timer: {value: Timer},
        skillByKey: {value: Resources.skill},
        itemById: {value: Resources.item},
        sendMessage: {value: Chat.sendMessage}
    });

    document.registerElement('dzida-game', {prototype: gameTag});
});

