define(function (require) {
    const uiState = require('../store/ui-state');
    const userEventStream = require('../component/dispatcher').userEventStream;

    const extraComponents = require('extra-ui');
    const fragments = [
        require('./ui-fragments/profiling-display'),
        require('./ui-fragments/respawnScreen'),
        require('./ui-fragments/cooldown-bar'),
        require('./ui-fragments/game-message'),
        require('./ui-fragments/server-error'),
        require('./ui-fragments/screen/loading-screen'),
        require('./ui-fragments/screen/disconnected-screen'),
        require('./ui-fragments/screen/connecting'),
        require('./ui-fragments/screen/connecting-to-instnace'),
        require('./ui-fragments/chat-ui'),
        require('./ui-fragments/inventory'),
        require('./ui-fragments/objectives-tracker'),
        require('./ui-fragments/current-parcel'),
        require('./ui-fragments/cursor'),
        require('./ui-fragments/casting-bar'),
        require('./ui-fragments/feedback-button')
    ].concat(extraComponents.fragments);

    const windows = [
        require('./windows/friendship-request-window'),
        require('./windows/friends-window'),
        require('./windows/feedback-window'),
        require('./windows/thank-you-window'),
        require('./windows/building-window'),
        require('./windows/parcel-window'),
        require('./windows/cooking-window'),
        require('./windows/start-quest-window'),
        require('./windows/complete-quest-window')
    ].concat(extraComponents.windows);

    const supportableRequirements = ['playerAlive', 'applicationState', 'instanceState', 'cooldown', 'gameMessage',
        'serverError', 'chatState', 'friendshipRequest', 'friendsConnectionState',
        'customCursor', 'casting', 'questToDisplay', 'activeQuests', 'completeQuestToDisplay',
        'isPlayerOnOwnParcel'];

    function initUi(gameUiElement) {
        const windowRegister = new Map();
        const uiFragmentsRegister = new Map();
        const windowActivateKeyBinds = new Map();

        const windowParentElement = gameUiElement.getElementsByClassName("window")[0];
        var windowElement = null;
        const uiFragmentsElement = gameUiElement.getElementsByClassName("ui-fragments")[0];

        const currentKeyBinds = new Map();

        const activeUiFragments = [];
        const activeUiFragmentElements = new Map();

        var activeWindow = null;
        var activeWindowElement = null;
        var closeWindowButton = (() => {
            const button = document.createElement('button');
            button.addEventListener('click', hideWindow);
            button.setAttribute('id', 'close-window-button');
            return button;
        })();


        windowParentElement.style.display = 'none';
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
            if (windowElement.detached) {
                windowElement.detached();
            }
            windowElement = null;
            windowParentElement.style.display = 'none';
            windowParentElement.innerHTML = '';
            activeWindow = null;
            activeWindowElement = null;
        }

        function hideWindow() {
            cleanWindow();
            setKeyBindings();
        }

        function showWindow(key) {
            if (activeWindow == key) {
                return;
            }
            if (activeWindow) {
                if (windowRegister.get(activeWindow).properties.closeable) {
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

            windowElement = createWindowElement(window);
            if (window.properties.closeable) {
                windowParentElement.appendChild(closeWindowButton);
            }

            if (windowElement.created) {
                windowElement.created();
            }
            windowParentElement.appendChild(windowElement);
            if (windowElement.attached) {
                windowElement.attached();
            }
            windowParentElement.style.display = 'block';
            activeWindowElement = windowParentElement;
            setKeyBindings();
        }

        function createWindowElement(window) {
            const windowElement = document.createElement('div');
            windowElement.setAttribute('id', window.key);
            return Object.assign(windowElement, window);
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
                if (shouldDisplay(windowRegister.get(windowKey).properties.requirements)) {
                    currentKeyBinds.set(shortCut, () => showWindow(windowKey))
                }
            });

            if (activeWindow != null) {
                const uiWindow = windowRegister.get(activeWindow).properties;

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
            const element = document.createElement('div');
            element.setAttribute('id', uiFragment.key);
            return Object.assign(element, uiFragment);
        }

        function displayUiFragment(fragmentKey) {
            activeUiFragments.push(fragmentKey);
            if (activeUiFragmentElements.has(fragmentKey)) {
                // ui fragment already displayed.
                return;
            }

            const uiFragment = uiFragmentsRegister.get(fragmentKey);
            const uiFragmentElement = createUiFragmentElement(uiFragment);
            if (uiFragmentElement.created) {
                uiFragmentElement.created();
            }

            uiFragmentsElement.appendChild(uiFragmentElement);
            if (uiFragmentElement.attached) {
                uiFragmentElement.attached();
            }
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
                if (element.detached) {
                    element.detached();
                }
                uiFragmentsElement.removeChild(element);
                activeUiFragmentElements.delete(fragmentKey)
            });
        }

        function renderUiFragments() {
            activeUiFragments.length = 0;
            const uiFragmentsToDisplay = [...uiFragmentsRegister.keys()].filter((key) => {
                const requirements = uiFragmentsRegister.get(key).properties.requirements;
                return shouldDisplay(requirements)
            });
            uiFragmentsToDisplay.forEach(displayUiFragment);
            hideNotDisplayedUiFragments();
        }

        function renderWindow() {
            const autoDisplayWindow = Array.from(windowRegister.values())
                .filter(uiWindow => uiWindow.properties.autoDisplay == true && shouldDisplay(uiWindow.properties.requirements));
            if (autoDisplayWindow.length > 1) {
                throw "can not display two auto displayable windows at the same time:" + JSON.stringify(autoDisplayWindow);
            }

            // first check if window is displayed and hide it if it is.
            // displaying of new window won't close  the previous one in case it was not closable
            if (activeWindow != null) {
                const uiWindow = windowRegister.get(activeWindow).properties;
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
            registerWindow: function (definition) {
                if (typeof definition.properties.closeable === 'undefined') {
                    definition.properties.closeable = true
                }
                validateRequirements(definition.properties.requirements, definition.key);

                windowRegister.set(definition.key, definition);

                if (definition.properties.activateKeyBind) {
                    windowActivateKeyBinds.set(definition.properties.activateKeyBind, definition.key);
                }
            },
            registerUiFragment: function (definition) {
                validateRequirements(definition.properties.requirements, definition.key);

                uiFragmentsRegister.set(definition.key, definition);
            },
            updateUi
        }
    }

    return {
        init(uiElement) {
            uiElement.innerHTML = `<div class="ui-fragments"></div><div class="window area"></div>`;
            const gameUi = initUi(uiElement);
            fragments.forEach(gameUi.registerUiFragment);
            windows.forEach(gameUi.registerWindow);
            gameUi.updateUi();
        }
    }
});