define((require) => {
    const uiState = require('../store/ui-state');
    const userEventStream = require('../component/dispatcher').userEventStream;
    const KeyCodes = require('../common/key-codes');

    const extraComponents = require('extra-ui');
    const fragments = [
        require('./ui-fragments/profiling-display'),
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
        const uiFragmentsElement = gameUiElement.getElementsByClassName("ui-fragments")[0];

        const currentKeyBinds = new Map();
        const activeUiFragmentElements = new Map();

        var activeWindow = null;
        var windowElement = null;
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
            const windowDefinition = windowRegister.get(activeWindow);
            if (windowDefinition.detached) {
                windowDefinition.detached(windowElement);
            }
            windowParentElement.style.display = 'none';
            windowParentElement.innerHTML = '';
            activeWindow = null;
            windowElement = null;
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
                if (windowRegister.get(activeWindow).closeable) {
                    cleanWindow();
                } else {
                    return;
                }
            }
            const windowDefinition = windowRegister.get(key);
            if (!windowDefinition) {
                throw `window ${key} does not exists`;
            }

            activeWindow = key;

            windowElement = createUiElement(windowDefinition);
            if (windowDefinition.closeable) {
                windowParentElement.appendChild(closeWindowButton);
            }

            windowParentElement.appendChild(windowElement);
            if (windowDefinition.attached) {
                windowDefinition.attached(windowElement);
            }
            windowParentElement.style.display = 'block';
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
                    currentKeyBinds.set(KeyCodes.ESC, hideWindow);
                }
                if (uiWindow.activateKeyBind) {
                    currentKeyBinds.set(uiWindow.activateKeyBind, hideWindow);
                }
            }
        }

        function createUiElement(uiFragment) {
            const element = document.createElement('div');
            element.setAttribute('id', uiFragment.key);
            if (uiFragment.template) {
                element.innerHTML = uiFragment.template;
            }
            if (uiFragment.classes) {
                uiFragment.classes.forEach(cssClass => element.classList.add(cssClass))
            }
            return element
        }

        function shouldDisplay(requirements) {
            if (!requirements) return true;

            return Object.keys(requirements).every(key => {
                return requirements[key](uiState[key].value);
            });
        }

        function displayUiFragment(fragmentKey) {
            if (activeUiFragmentElements.has(fragmentKey)) {
                // element is already displayed
                return;
            }

            const fragmentDefinition = uiFragmentsRegister.get(fragmentKey);
            const element = createUiElement(fragmentDefinition);
            uiFragmentsElement.appendChild(element);
            if (fragmentDefinition.attached) {
                fragmentDefinition.attached(element);
            }
            activeUiFragmentElements.set(fragmentKey, element);
        }

        function hideUiFragment(key) {
            if (!activeUiFragmentElements.has(key)) {
                // element is not displayed
                return
            }

            const fragmentDefinition = uiFragmentsRegister.get(key);
            const element = activeUiFragmentElements.get(key);
            if (fragmentDefinition.detached) {
                fragmentDefinition.detached(element);
            }
            uiFragmentsElement.removeChild(element);
            activeUiFragmentElements.delete(key)
        }

        function renderUiFragments() {
            [...uiFragmentsRegister.keys()].forEach((key) => {
                const requirements = uiFragmentsRegister.get(key).requirements;
                if (shouldDisplay(requirements)) {
                    displayUiFragment(key)
                } else {
                    hideUiFragment(key)
                }
            });
        }

        function renderWindow() {
            const autoDisplayWindow = Array.from(windowRegister.values())
                .filter(uiWindow => uiWindow.autoDisplay == true && shouldDisplay(uiWindow.requirements));
            if (autoDisplayWindow.length > 1) {
                throw "can not display two auto displayable windows at the same time:" + JSON.stringify(autoDisplayWindow);
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
            registerWindow: function (definition) {
                if (typeof definition.closeable === 'undefined') {
                    definition.closeable = true
                }
                validateRequirements(definition.requirements, definition.key);

                windowRegister.set(definition.key, definition);

                if (definition.activateKeyBind) {
                    windowActivateKeyBinds.set(definition.activateKeyBind, definition.key);
                }
            },
            registerUiFragment: function (definition) {
                validateRequirements(definition.requirements, definition.key);

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