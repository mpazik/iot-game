var GAME_UI;


(function () {
    const KeyCodes = {
        ESC: 27,
        ENTER: 13
    };

    var gameUi;
    const windowRegister = new Map();
    const uiKeyBinds = new Map();

    function getCharCode(key) {
        if (typeof key == "string" && key.length == 1) {
            return key.charCodeAt(0);
        }
        if (typeof key == "number") {
            return key;
        }
        throw "key bing should be either a letter or a key code";
    }

    function initUi(gameUiElement) {
        const windowElement = document.getElementById("window");
        const currentKeyBinds = new Map();
        var currentWindow = null;
        document.addEventListener('keydown', keyListener);
        hideWindow();

        function keyListener(event) {
            const binding = currentKeyBinds.get(event.keyCode);
            if (binding) {
                binding();
            }
        }

        function hideWindow() {
            windowElement.style.display = 'none';
            currentWindow = null;
            setUiKeyBindings();
            windowElement.innerHTML = '';
        }

        function showWindow(key) {
            if (currentWindow) {
                hideWindow();
            }
            const window = windowRegister.get(key);
            if (!window) {
                throw `window ${key} does not exists`;
            }
            currentWindow = key;
            currentKeyBinds.clear();
            if (window.keyBinds) {
                Object.forEach(window.keyBinds, function (binding, key) {
                    currentKeyBinds.set(getCharCode(key), binding)
                });
            }
            currentKeyBinds.set(KeyCodes.ESC, hideWindow);
            const windowInstance = document.createElement(window.tagName);
            windowElement.appendChild(windowInstance);
            windowElement.style.display = 'block';
        }

        function setUiKeyBindings() {
            currentKeyBinds.clear();
            uiKeyBinds.forEach((binding, key) => currentKeyBinds.set(key, binding));
        }

        return {
            refreshUiAfterRegisteringWindow: () => {
                if (currentWindow == null) {
                    setUiKeyBindings()
                }
            },
            showWindow: showWindow
        }
    }

    window.addEventListener("game-ui attached", (event) => {
        gameUi = initUi(event.srcElement);
        console.log("Game UI Initialized");
    });

    function showWindow(windowKey) {
        if (gameUi) {
            gameUi.showWindow(windowKey);
        }
    }

    function createActivateWindowAction(windowKey) {
        return function () {
            showWindow(windowKey)
        }
    }


    GAME_UI = {
        registerWindow: function (key, params) {
            if (typeof params == 'undefined') {
                params = {};
            }
            windowRegister.set(key, params);
            if (!params.tagName || !(params.tagName instanceof "string")) {
                params.tagName = key
            }
            var activateKeyBind = params.activateKeyBind;
            if (activateKeyBind) {
                uiKeyBinds.set(getCharCode(activateKeyBind), createActivateWindowAction(key));
            }
            if (gameUi) {
                gameUi.refreshUiAfterRegisteringWindow();
            }
        },
        showWindow
    }
})();