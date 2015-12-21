var GAME_UI;


(function () {
    const KeyCodes = {
        ESC: 27,
        ENTER: 13
    };

    var gameUi;
    const windowRegister = new Map();
    const uiFragmentsRegister = new Map();
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
        const windowElement = gameUiElement.getElementsByClassName("window")[0];
        const uiFragmentsElement = gameUiElement.getElementsByClassName("ui-fragments")[0];
        const currentKeyBinds = new Map();
        const activeUiFragments = [];
        const activeUiFragmentElements = new Map();
        var currentWindow = null;
        var uiState = {};

        gameUiElement.addEventListener('ui-state-updated', (event) => updateState(event.detail));
        document.addEventListener('keydown', keyListener);
        defaultWindowKeyBinds.set(KeyCodes.ESC, showUi);
        renderUiFragments();
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

        function createUiFragmentElement(uiFragment) {
            const uiFragmentInstance = document.createElement(uiFragment.tagName);
            if (uiFragment.location === 'center') {
                const centerWrapper = document.createElement('div');
                centerWrapper.classList.add('center');
                centerWrapper.appendChild(uiFragmentInstance);
                return centerWrapper
            } else {
                return uiFragmentInstance
            }
        }

        function displayUiFragment(fragmentKey) {
            if (activeUiFragmentElements.has(fragmentKey)){
                // ui fragment already displayed.
                return;
            }

            const uiFragment = uiFragmentsRegister.get(fragmentKey);
            const uiFragmentElement = createUiFragmentElement(uiFragment);

            uiFragmentsElement.appendChild(uiFragmentElement);
            activeUiFragmentElements.set(fragmentKey, uiFragmentElement);
            activeUiFragments.push(fragmentKey);
        }

        function shouldDisplayUiFragment(fragmentKey) {
            const requirements = uiFragmentsRegister.get(fragmentKey).requirements;
            if (!requirements) return true;

            return Object.keys(requirements).every(key => {
                const requiredValue = requirements[key];
                return uiState[key] === requiredValue;
            });
        }

        function hideNotDisplayedUiFragments() {
            const removedUiFragments = [...activeUiFragmentElements.keys()].filter(key => {
                return !activeUiFragments.includes(key);
            });
            removedUiFragments.forEach(fragmentKey => {
                const element = activeUiFragmentElements.get(fragmentKey);
                uiFragmentsElement.removeChild(element);
                activeUiFragmentElements.delete(fragmentKey)
            });
        }

        function renderUiFragments() {
            activeUiFragments.clear();
            const uiFragmentsToDisplay = [...uiFragmentsRegister.keys()].filter((key) => {
                return shouldDisplayUiFragment(key)
            });
            uiFragmentsToDisplay.forEach(displayUiFragment);
            hideNotDisplayedUiFragments();
        }

        function updateState(state) {
            uiState = state;
            renderUiFragments();
        }

        return {
            refreshUiAfterRegisteringWindow: () => {
                // window registration might have registered uiKeyBinds or defaultWindowKeyBinds, that's why we need to refresh keyBindings.
                if (currentWindow) {
                    setWindowKeyBindings(windowRegister.get(currentWindow))
                } else {
                    setUiKeyBindings();
                }
            },
            refreshUiAfterRegisteringUiFragment: () => {
                // window registration might have registered uiKeyBinds, that's why we need to refresh keyBindings.
                if (!currentWindow) {
                    setUiKeyBindings();
                }
                renderUiFragments();
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
            if (!params.tagName || !(params.tagName instanceof "string")) {
                params.tagName = key
            }

            windowRegister.set(key, params);

            if (params.activateKeyBind) {
                var showWindow = createActivateWindowAction(key);
                uiKeyBinds.set(getCharCode(params.activateKeyBind), showWindow);
                defaultWindowKeyBinds.set(getCharCode(params.activateKeyBind), showWindow);
            }
            if (gameUi) {
                gameUi.refreshUiAfterRegisteringWindow();
            }
        },
        registerUiFragment: function (key, params) {
            if (typeof params == 'undefined') {
                params = {};
            }
            if (!params.tagName || !(params.tagName instanceof "string")) {
                params.tagName = key
            }

            uiFragmentsRegister.set(key, params);

            if (gameUi) {
                gameUi.refreshUiAfterRegisteringUiFragment();
            }
        },
        showWindow
    }
})();