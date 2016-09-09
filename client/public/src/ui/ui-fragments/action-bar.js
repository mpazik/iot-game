define(function (require) {
    require('../elements/action-socket');
    const skillByKey = require('../../store/resources').skill;
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const ActionBar = require('../../store/action-bar');

    const keyBinds = ['Q', 'W', 'E', 'R', '1', '2', '3', '4', '5'];

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
            this._renderSystemBar();
            this._updateActive(ActionBar.activeState.value);
            this._renderSkillBar(ActionBar.skills.value);
            ActionBar.skills.subscribe(this._renderSkillBar.bind(this));
            ActionBar.activeState.subscribe(this._updateActive.bind(this));
        },
        detached: function () {
            ActionBar.skills.unsubscribe(this._renderSkillBar.bind(this));
            ActionBar.activeState.unsubscribe(this._updateActive.bind(this));
        },
        _renderSystemBar: function () {
            function createSystemIcon(key, icon, keyBind, title, action) {
                var socket = document.createElement('action-socket');
                if (icon) socket.setAttribute('icon', icon);
                if (key) socket.setAttribute('key', key);
                if (action) socket.addEventListener('action-triggered', action);
                if (keyBind) socket.setAttribute('keyBind', keyBind);
                if (title) socket.setAttribute('title', title);
                return socket;
            }

            const systemBar = this.getElementsByClassName("system-bar")[0];
            var leaderboardButton = createSystemIcon('leaderboard', 'icon-ranking', 'L', 'Leaderboard', function () {
                userEventStream.publish('toggle-window', 'leaderboard-window');
            });
            systemBar.appendChild(leaderboardButton);

            var settingsButton = createSystemIcon('achievements', 'icon-achievement', 'A', 'Achievements', function () {
                userEventStream.publish('toggle-window', 'achievement-window');
            });
            systemBar.appendChild(settingsButton);
        },
        _renderSkillBar: function (skillIds) {
            const skillBar = this.getElementsByClassName("skill-bar")[0];
            const skills = skillIds.map(function (id) {
                if (id == null) return;
                return skillByKey(id);
            });
            skillBar.innerHTML = '';
            skills.forEach(function (skill, index) {
                var socket = document.createElement('action-socket');
                if (skill) {
                    socket.setAttribute('icon', 'icon-' + skill.icon);
                    socket.setAttribute('key', skill.id);
                    socket.setAttribute('title', skill.name);
                    socket.addEventListener('action-triggered', function (event) {
                        const skill = skillByKey(event.detail.key);
                        userEventStream.publish('skill-triggered', {skill});
                    });
                }
                socket.setAttribute('keyBind', keyBinds[index]);
                skillBar.appendChild(socket);
            });
        },
        _updateActive: function (active) {
            const sockets = Array.prototype.slice.call(this.getElementsByTagName('action-socket'));

            const findActiveSkillSocket = () =>sockets.find(socket => socket.getAttribute('key') == this.activeSkill);

            if (this.activeSkill != null) {
                findActiveSkillSocket().removeAttribute('active');
            }

            if (active != null) {
                this.activeSkill = active;
                findActiveSkillSocket().setAttribute('active', 'active');
            }
        }
    });

});