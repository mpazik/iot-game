define(function (require) {
    const uiState = require('../store/ui-state');
    const userEventStream = require('../component/dispatcher').userEventStream;

    const extraComponents = require('./extra');
    const fragments = [
        require('./ui-fragments/profiling-display'),
        require('./ui-fragments/respawnScreen'),
        require('./ui-fragments/join-battle-button'),
        require('./ui-fragments/action-bar'),
        require('./ui-fragments/cooldown-bar'),
        require('./ui-fragments/game-message'),
        require('./ui-fragments/screen/loading-screen'),
        require('./ui-fragments/screen/disconnected-screen'),
        require('./ui-fragments/screen/connecting'),
        require('./ui-fragments/chat-ui'),
        require('./ui-fragments/inventory')
    ].concat(extraComponents.fragments);

    const windows = [
        require('./windows/join-battle-window'),
        require('./windows/survival-end-victory-window'),
        require('./windows/survival-end-defeat-window'),
        require('./windows/login-window'),
        require('./windows/settings-window'),
        require('./windows/leaderboard-window')
    ].concat(extraComponents.windows);

    var gameUiTag = Object.create(HTMLElement.prototype, {
        createdCallback: {
            value: function () {
                this.innerHTML = `<div class="ui-fragments"></div><div class="window area"></div>`;
            }
        },
        attachedCallback: {
            value: function () {
                // because element is created asynchronously due to use requireJs we need to emit event when it's has been propertly created.
                this.dispatchEvent(new CustomEvent('element-attached'))
            }
        },
        init: {
            value: function () {
                const gameUi = initUi(this);
                fragments.forEach(function (tag) {
                    gameUi.registerUiFragment(tag.name, tag.prototype.properties);
                });
                windows.forEach(function (tag) {
                    gameUi.registerWindow(tag.name, tag.prototype.properties);
                });
                gameUi.updateUi();
            }
        }
    });
    document.registerElement('game-ui', {prototype: gameUiTag});

    const supportableRequirements = ['playerAlive', 'scenarioType', 'scenarioResolution',
        'endScenario', 'applicationState', 'instanceState', 'cooldown', 'gameMessage', 'chatState'];

    function initUi(gameUiElement) {
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

        userEventStream.subscribe('toggle-window', toggleWindow);

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

        function toggleWindow(key) {
            if (activeWindow == key) {
                hideWindow();
            } else {
                showWindow(key);
            }
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

            // first check if window is displayed and hide it if it is.
            // displaying of new window won't close  the previous one in case it was not closable
            if (activeWindow != null) {
                const uiWindow = windowRegister.get(activeWindow);
                if (!shouldDisplay(uiWindow.requirements)) {
                    hideWindow();
                }
            }

            if (autoDisplayWindow[0]) {
                showWindow(autoDisplayWindow[0].key);
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
            updateUi
        }
    }
});