var GAME_UI;


(function () {
    const KeyCodes = {
        ESC: 27,
        ENTER: 13
    };

    var gameUi;
    const windowRegister = new Map();
    const uiFragmentsRegister = new Map();
    const windowActivateKeyBinds = new Map();

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
        var activeWindow = null;
        var activeWindowElement = null;
        var uiState = {};

        windowElement.style.display = 'none';
        gameUiElement.addEventListener('ui-state-updated', (event) => updateState(event.detail));
        document.addEventListener('keydown', keyListener);
        renderUiFragments();

        function keyListener(event) {
            const binding = currentKeyBinds.get(event.keyCode);
            if (binding) {
                binding();
            }
        }

        function cleanWindow() {
            windowElement.style.display = 'none';
            windowElement.removeChild(activeWindowElement);
            activeWindow = null;
            activeWindowElement = null;
        }

        function hideWindow() {
            cleanWindow();
            setKeyBindings();
        }

        function showWindow(key) {
            if (activeWindow) {
                cleanWindow();
            }
            const window = windowRegister.get(key);
            if (!window) {
                throw `window ${key} does not exists`;
            }

            activeWindow = key;

            const windowInstance = document.createElement(window.tagName);
            windowElement.appendChild(windowInstance);
            windowElement.style.display = 'block';
            activeWindowElement = windowInstance;
            setKeyBindings();
        }

        function setKeyBindings() {
            currentKeyBinds.clear();
            windowActivateKeyBinds.forEach((windowKey, shortCut) => {
                if (shouldDisplay(windowRegister.get(windowKey).requirements)) {
                    currentKeyBinds.set(shortCut, () => showWindow(windowKey))
                }
            });

            if (activeWindow != null) {
                const window = windowRegister.get(activeWindow);
                currentKeyBinds.set(KeyCodes.ESC, hideWindow);
                if (window.keyBinds) {
                    Object.forEach(window.keyBinds, function (binding, key) {
                        currentKeyBinds.set(getCharCode(key), activeWindowElement[binding].bind(activeWindowElement))
                    });
                }
                if (window.activateKeyBind) {
                    currentKeyBinds.set(getCharCode(window.activateKeyBind), hideWindow);
                }
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
            if (activeUiFragmentElements.has(fragmentKey)) {
                // ui fragment already displayed.
                return;
            }

            const uiFragment = uiFragmentsRegister.get(fragmentKey);
            const uiFragmentElement = createUiFragmentElement(uiFragment);

            uiFragmentsElement.appendChild(uiFragmentElement);
            activeUiFragmentElements.set(fragmentKey, uiFragmentElement);
            activeUiFragments.push(fragmentKey);
        }

        function shouldDisplay(requirements) {
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
                const requirements = uiFragmentsRegister.get(key).requirements;
                return shouldDisplay(requirements)
            });
            uiFragmentsToDisplay.forEach(displayUiFragment);
            hideNotDisplayedUiFragments();
        }

        function renderWindow() {
            if (activeWindow == null) return;

            const window = windowRegister.get(activeWindow);
            if (!shouldDisplay(window.requirements)) {
                hideWindow();
            }
        }

        function updateState(state) {
            uiState = state;
            renderUiFragments();
            renderWindow();
            setKeyBindings();
        }

        return {
            refreshUiAfterRegisteringWindow: () => {
                // window registration might have registered uiKeyBinds or windowActivateKeyBinds, that's why we need to refresh keyBindings.
                setKeyBindings();
            },
            refreshUiAfterRegisteringUiFragment: () => {
                // window registration might have registered uiKeyBinds, that's why we need to refresh keyBindings.
                setKeyBindings();
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
                windowActivateKeyBinds.set(getCharCode(params.activateKeyBind), key);
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