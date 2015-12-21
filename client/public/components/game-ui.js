var GAME_UI;


(function () {
    const KeyCodes = {
        ESC: 27,
        ENTER: 13
    };

    var gameUi;
    const windowRegister = new Map();
    const uiKeyBinds = new Map();
    const defaultWindowKeyBinds = new Map();

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
        defaultWindowKeyBinds.set(KeyCodes.ESC, showUi);
        showUi();

        function keyListener(event) {
            const binding = currentKeyBinds.get(event.keyCode);
            if (binding) {
                binding();
            }
        }

        function cleanWindow() {
            windowElement.style.display = 'none';
            currentWindow = null;
            windowElement.innerHTML = '';
        }

        function showUi() {
            cleanWindow();
            setUiKeyBindings();
        }

        function showWindow(key) {
            if (currentWindow) {
                cleanWindow();
            }
            const window = windowRegister.get(key);
            if (!window) {
                throw `window ${key} does not exists`;
            }

            currentWindow = key;
            setWindowKeyBindings(window);

            const windowInstance = document.createElement(window.tagName);
            windowElement.appendChild(windowInstance);
            windowElement.style.display = 'block';
        }

        function setUiKeyBindings() {
            currentKeyBinds.clear();
            uiKeyBinds.forEach((binding, key) => currentKeyBinds.set(key, binding));
        }

        function setWindowKeyBindings(window) {
            currentKeyBinds.clear();
            defaultWindowKeyBinds.forEach((binding, key) => currentKeyBinds.set(key, binding));

            if (window.keyBinds) {
                Object.forEach(window.keyBinds, function (binding, key) {
                    currentKeyBinds.set(getCharCode(key), binding)
                });
            }
            if (window.activateKeyBind) {
                currentKeyBinds.set(getCharCode(window.activateKeyBind), showUi);
            }
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
            if (params.activateKeyBind) {
                var showWindow = createActivateWindowAction(key);
                uiKeyBinds.set(getCharCode(params.activateKeyBind), showWindow);
                defaultWindowKeyBinds.set(getCharCode(params.activateKeyBind), showWindow);
            }
            if (gameUi) {
                gameUi.refreshUiAfterRegisteringWindow();
            }
        },
        showWindow
    }
})();