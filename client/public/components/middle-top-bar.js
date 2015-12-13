define(function (require, exports, module) {
    const Dispatcher = require('../src/component/dispatcher');

    const id = 'middle-top-bar-content';
    var parent;

    const joinBattleButton = (() => {
        const element = document.createElement('button');
        element.innerHTML = 'Join Battle!';
        element.id = 'select-battle-button';
        element.onclick = event => {
            event.stopPropagation();
            selectBattle();
        };
        return element;
    })();

    const joinBattleWindow = (() => {
        const element = document.createElement('div');
        element.innerHTML = `<select id="join-battle-select">
    <option value="small-island">Small Island - Survival</option>
</select>`;
        element.id = 'join-battle-window';
        element.className = 'window';
        element.onclick = function (event) {
            event.stopPropagation();
        };

        const button = document.createElement('button');
        button.id = 'join-battle-button';
        button.innerHTML = 'Join!';
        button.onclick = function () {
            const map = document.getElementById('join-battle-select').value;
            Dispatcher.userEventStream.publish('join-battle', map);
            setState({type: 'joining'})
        };
        element.appendChild(button);
        return element;
    })();

    const joining = (() => {
        const element = document.createElement('span');
        element.innerHTML = "Joining...";
        return element;
    })();

    function selectBattle() {
        setState({type: 'window'})
    }

    function closeWindow() {
        setState({type: 'normal'})
    }

    function getChild(state) {
        switch (state.type) {
            case 'normal':
                return joinBattleButton;
            case 'window':
                var listener = event => {
                    if (event.target.id == 'join-battle-window' ||
                        event.target.id == 'join-battle-select' ||
                        event.target.id == 'join-battle-button') {
                        return;
                    }
                    closeWindow();
                    Dispatcher.userEventStream.unsubscribe('left-click', listener);
                };
                Dispatcher.userEventStream.subscribe('left-click', listener);
                return joinBattleWindow;
            case 'joining':
                return joining;
        }
    }

    function setState(newState) {
        parent.innerHTML = '';
        parent.appendChild(getChild(newState));

    }

    return {
        init: () => {
            parent = document.getElementById(id);
            setState({type: 'normal'});
        }
    };
});