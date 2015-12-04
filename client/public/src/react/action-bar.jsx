define(function (require) {
    const React = require('lib/react');
    const ActionBarStore = require('../store/action-bar');
    const SkillStore = require('../store/skill');
    const Dispatcher = require('../component/dispatcher');

    const SkillSocket = React.createClass({
        componentDidMount: function () {
            Dispatcher.keyPressStream.subscribe(this.props.keyBind, this._onIconClick);
        },

        componentWillUnmount: function () {
            Dispatcher.keyPressStream.unsubscribe(this.props.keyBind, this._onIconClick);
        },

        render: function () {
            var classes = 'icon';
            if (this.props.skill && this.props.skill.icon) {
                classes += ' ' + this.props.skill.icon;
            } else {
                classes += ' none';
            }
            if (this.props.active) {
                classes += ' active';
            }
            return (
                <div id={this.props.id} className={classes} onClick={this._onIconClick}>
                    <div className="key-bind">{this.props.keyBind}</div>
                </div>
            );
        },

        _onIconClick: function () {
            if (!this.props.skill) {
                return;
            }
            Dispatcher.userEventStream.publish({
                type: 'skill-triggered',
                skill: this.props.skill
            });
        }
    });

    const keyBinds = ['Q', 'W', 'E', 'R', '1', '2', '3', '4', '5'];

    const SkillBar = React.createClass({
        render: function () {
            const activeSkill = this.props.active;
            const skillSockets = this.props.skills.map(function (skill, index) {
                const keyBind = keyBinds[index];
                const id = 'sb-' + index;
                const active = index === activeSkill;
                return <SkillSocket key={id} id={id} skill={skill} keyBind={keyBind} active={active}/>
            });
            return (<div id="skill-bar" className="icon-socket-set">
                {skillSockets}
            </div>)
        }
    });

    return React.createClass({
        getInitialState: function () {
            const skills = ActionBarStore.skills.map(function (id) {
                return SkillStore.skill(id);
            });
            const active = ActionBarStore.activeState.value;
            return {
                skills: skills,
                active: active
            };
        },

        componentDidMount: function () {
            ActionBarStore.activeState.subscribe(this._updateActive);
        },

        componentWillUnmount: function () {
            ActionBarStore.activeState.unsubscribe(this._updateActive);
        },

        render: function () {
            return (
                <div id="action-bar">
                    <SkillBar skills={this.state.skills} active={this.state.active}/>
                </div>
                //#skill_bar.icon_socket_set
                //  //- #sb-main_hand: .key_bind LPM
                //  each key, i in
                //  div(id="sb-"+i): .key_bind #{key}
                //
                //  #menu_bar.icon_socket_set
                //  #menu_bar_inventory.icon_sys.sys_inventory: .key_bind I
                //  #menu_bar_skill.icon_sys.sys_skill: .key_bind S
                //  #menu_bar_character.icon_sys.sys_character: .key_bind C
                //  #menu_bar_help.icon_sys.sys_help: .key_bind H
            );
        },

        _updateActive: function (active) {
            const state = this.state;
            state.active = active;
            this.setState(state);
        }
    });
});