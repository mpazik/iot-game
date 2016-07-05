define(function (require, exports, module) {
    require('../elements/action-socket');
    const uiState = require('../../store/ui-state');
    const skillByKey = require('../../store/resources').skill;
    const userEventStream = require('../../component/dispatcher').userEventStream;

    return createUiElement('action-bar', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true)
            }
        },

        created: function () {
            this.innerHTML = `
<div class="skill-bar icon-socket-set"></div>
<div class="system-bar icon-socket-set"></div>
`;
        },

        attached: function () {
            const keyBinds = ['Q', 'W', 'E', 'R', '1', '2', '3', '4', '5'];
            const actionBar = this;

            function createSystemIcon(key, icon, keyBind, action) {
                var socket = document.createElement('action-socket');
                if (icon) socket.setAttribute('icon', icon);
                if (key) socket.setAttribute('key', key);
                if (action) socket.addEventListener('action-triggered', action);
                if (keyBind) socket.setAttribute('keyBind', keyBind);
                return socket;
            }

            // init system bar
            const systemBar = this.getElementsByClassName("system-bar")[0];
            var settingsButton = createSystemIcon('settings', 'icon-settings', 'S', function () {
                userEventStream.publish('toggle-window', 'settings-window');
            });
            systemBar.appendChild(settingsButton);

            var leaderboardButton = createSystemIcon('settings', 'icon-ranking', 'L', function () {
                userEventStream.publish('toggle-window', 'leaderboard-window');
            });
            systemBar.appendChild(leaderboardButton);

            // init skill bar
            const skillBar = this.getElementsByClassName("skill-bar")[0];
            const skills = uiState.actionBarSkills.value.map(function (id) {
                if (id == null) return;
                return skillByKey(id);
            });
            skills.forEach(function (skill, index) {
                var socket = document.createElement('action-socket');
                if (skill) {
                    socket.setAttribute('icon', 'icon-' + skill.icon);
                    socket.setAttribute('key', skill.id);
                    socket.addEventListener('action-triggered', function (event) {
                        const skill = skillByKey(event.detail.key);
                        userEventStream.publish('skill-triggered', {skill});
                    });
                }
                socket.setAttribute('keyBind', keyBinds[index]);
                skillBar.appendChild(socket);
            });
            this._updateActive(uiState.actionBarActiveSkill.value);
            uiState.actionBarActiveSkill.subscribe(this._updateActive.bind(this));
        },
        detached: function () {
            uiState.actionBarActiveSkill.unsubscribe(this._updateActive.bind(this));
        },
        _updateActive: function (active) {
            const sockets = this.getElementsByTagName('action-socket');
            if (this.activeSkill != null) {
                sockets[this.activeSkill].removeAttribute('active');
            }
            if (active != null) {
                this.activeSkill = active;
                sockets[active].setAttribute('active', 'active');
            }
        }
    });

});