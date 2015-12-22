define([], function () {
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
        uiState.playerAlive.subscribe(updateUi);
        uiState.scenarioType.subscribe(updateUi);

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
                //noinspection AmdModulesDependencies
                currentKeyBinds.set(KEY_CODES.ESC, hideWindow);
                if (window.keyBinds) {
                    window.keyBinds.forEach(function (entry) {
                        const key = entry[0];
                        const binding = entry[1];
                        currentKeyBinds.set(key, activeWindowElement[binding].bind(activeWindowElement))
                    });
                }
                if (window.activateKeyBind) {
                    currentKeyBinds.set(window.activateKeyBind, hideWindow);
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
                return uiState[key].value === requiredValue;
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

        function updateUi () {
            renderUiFragments();
            renderWindow();
            setKeyBindings();
        }

        return {
            registerWindow: function (key, params) {
                if (typeof params == 'undefined') {
                    params = {};
                }
                if (!params.tagName) {
                    params.tagName = key
                }

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