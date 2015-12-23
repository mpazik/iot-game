define([], function () {
    const supportableRequirements = ['playerAlive', 'scenarioType', 'endScenario', 'applicationState', 'cooldown', 'gameMessage'];

    function initUi(gameUiElement, uiState) {
        const windowRegister = new Map();
        const uiFragmentsRegister = new Map();
        const windowActivateKeyBinds = new Map();

        const windowElement = gameUiElement.getElementsByClassName("window")[0];
        const uiFragmentsElement = gameUiElement.getElementsByClassName("ui-fragments")[0];

        const currentKeyBinds = new Map();

        const activeUiFragments = [];
        const activeUiFragmentElements = new Map();

        var activeWindow = null;
        var activeWindowElement = null;

        windowElement.style.display = 'none';
        document.addEventListener('keydown', keyListener);
        supportableRequirements.forEach(requirements => {
            uiState[requirements].subscribe(updateUi);
        });

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
                if (windowRegister.get(activeWindow).closeable) {
                    cleanWindow();
                } else {
                    return;
                }
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
                const uiWindow = windowRegister.get(activeWindow);

                if (uiWindow.closeable) {
                    //noinspection AmdModulesDependencies
                    currentKeyBinds.set(KEY_CODES.ESC, hideWindow);
                }
                if (uiWindow.keyBinds) {
                    uiWindow.keyBinds.forEach(function (entry) {
                        const key = entry[0];
                        const binding = entry[1];
                        currentKeyBinds.set(key, activeWindowElement[binding].bind(activeWindowElement))
                    });
                }
                if (uiWindow.activateKeyBind) {
                    currentKeyBinds.set(uiWindow.activateKeyBind, hideWindow);
                }
            }
        }

        function createUiFragmentElement(uiFragment) {
            return document.createElement(uiFragment.tagName);
        }

        function displayUiFragment(fragmentKey) {
            activeUiFragments.push(fragmentKey);
            if (activeUiFragmentElements.has(fragmentKey)) {
                // ui fragment already displayed.
                return;
            }

            const uiFragment = uiFragmentsRegister.get(fragmentKey);
            const uiFragmentElement = createUiFragmentElement(uiFragment);

            uiFragmentsElement.appendChild(uiFragmentElement);
            activeUiFragmentElements.set(fragmentKey, uiFragmentElement);
        }

        function shouldDisplay(requirements) {
            if (!requirements) return true;

            return Object.keys(requirements).every(key => {
                return requirements[key](uiState[key].value);
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
            const autoDisplayWindow = windowRegister.filterValues(uiWindow => uiWindow.autoDisplay == true && shouldDisplay(uiWindow.requirements));
            if (autoDisplayWindow.length > 1) {
                throw "can not display two auto displayable windows at the same time";
            }
            if (autoDisplayWindow[0]) {
                showWindow(autoDisplayWindow[0].key);
                return
            }

            if (activeWindow == null) return;

            const uiWindow = windowRegister.get(activeWindow);
            if (!shouldDisplay(uiWindow.requirements)) {
                hideWindow();
            }
        }

        function updateUi() {
            renderUiFragments();
            renderWindow();
            setKeyBindings();
        }

        function validateRequirements(requirements, key) {
            if (!requirements) return;

            Object.keys(requirements).forEach(requirement => {
                if (typeof requirements[requirement] !== 'function') {
                    throw `Requirement <[${requirement}]> of element <[${key}]> has to be function`;
                }

                if (!supportableRequirements.includes(requirement)) {
                    throw `Requirement <[${requirement}]> of element <[${key}]> is not supported. \n` +
                    `List of supported requirements: ${supportableRequirements}`
                }
            });
        }

        return {
            registerWindow: function (key, params) {
                if (typeof params == 'undefined') {
                    params = {};
                }
                params.key = key;

                if (typeof params.tagName === 'undefined') {
                    params.tagName = key
                }
                if (typeof params.closeable === 'undefined') {
                    params.closeable = true
                }
                validateRequirements(params.requirements, key);

                windowRegister.set(key, params);

                if (params.activateKeyBind) {
                    windowActivateKeyBinds.set(params.activateKeyBind, key);
                }
            },
            registerUiFragment: function (key, params) {
                if (typeof params == 'undefined') {
                    params = {};
                }
                if (!params.tagName) {
                    params.tagName = key
                }
                validateRequirements(params.requirements, key);

                uiFragmentsRegister.set(key, params);
            },
            showWindow,
            updateUi
        }
    }

    return {
        create: initUi
    };
});