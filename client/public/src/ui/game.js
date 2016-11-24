define((require) => {
    const App = require('../component/application');
    const Dispatcher = require('../component/dispatcher');
    const KeyCodes = require('../common/key-codes');

    return {
        create() {
            const renderElement = document.createElement('div');
            renderElement.setAttribute('id', 'game-render');

            return renderElement;
        },
        start() {
            App.init(document.getElementById('game-render'));

            window.addEventListener('mousedown', function (event) {
                if (event.which && event.which == 1) {
                    Dispatcher.userEventStream.publish('left-click', event);
                }
                if (event.which && event.which == 3) {
                    Dispatcher.userEventStream.publish('right-click', event);
                }
            });

            window.addEventListener('keydown', function (event) {
                if (event.keyCode == KeyCodes.ESC) {
                    Dispatcher.userEventStream.publish('esc-down', event);
                }
            });
        }
    }
});

