define(function (require, exports, module) {
    const App = require('../component/application');
    const Dispatcher = require('../component/dispatcher');

    const gameTag = Object.create(HTMLElement.prototype, {
        createdCallback: {
            value: function () {
                this.innerHTML = `<div id="game-render"></div>`;
            }
        },
        attachedCallback: {
            value: function () {
                const gameRender = document.getElementById('game-render');
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
        }
    });

    document.registerElement('dzida-game', {prototype: gameTag});
});

