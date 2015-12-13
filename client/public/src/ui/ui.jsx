define(function (require) {
    const Profiling = require('jsx!./profiling');
    const ActionBar = require('jsx!./action-bar');
    const CooldownBar = require('jsx!./cooldown-bar');
    const Configuration = require('../component/configuration');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../store/server-messages');
    const MainPlayer = require('../store/main-player');
    const MiddleTopBarLogic = require('./middle-top-bar');

    function countLeft(middleBarWidth2) {
        const windowWidth = window.innerWidth;
        return windowWidth / 2 - middleBarWidth2 / 2;
    }

    const MiddleTopBar = React.createClass({
        _middleBarWidth: 100,
        handleResize: function (e) {
            this.setState();
        },

        componentDidMount: function () {
            window.addEventListener('resize', this.handleResize);
            MiddleTopBarLogic.init();
        },

        componentWillUnmount: function () {
            window.removeEventListener('resize', this.handleResize);
        },

        render: function () {
            return (
                <div id="middle-top-bar">
                    <div id="middle-top-bar-content"></div>
                </div>
            );
        }
    });

    const MiddleBottomBar = React.createClass({
        _middleBarWidth: 460,
        getInitialState: function () {
            return {left: countLeft(this._middleBarWidth)};
        },

        handleResize: function (e) {
            this.setState({left: countLeft(this._middleBarWidth)});
        },

        componentDidMount: function () {
            window.addEventListener('resize', this.handleResize);
        },

        componentWillUnmount: function () {
            window.removeEventListener('resize', this.handleResize);
        },

        render: function () {
            const style = {
                left: this.state.left
            };
            return (
                <div id="middle-bar" style={style}>
                    <ActionBar />
                </div>
            );
        }
    });

    const DeathScreen = React.createClass({
        spawnTimeOut: null,
        getInitialState: function () {
            this.spawnTimeOut = setTimeout(this._countDown, 1000);
            return {
                timeToRespawn: Configuration.spawnTime / 1000
            };
        },

        render: function () {
            return (
                <div id="death_screen">
                    <h1>Your character died.</h1>
                    <h3>They will respawn in {this.state.timeToRespawn}</h3>
                </div>
            );
        },

        componentWillUnmount: function () {
            clearTimeout(this.spawnTimeOut);
        },


        _countDown: function () {
            const timeToRespawn = this.state.timeToRespawn;
            this.setState({timeToRespawn: timeToRespawn - 1});
            if (timeToRespawn > 1) {
                this.spawnTimeOut = setTimeout(this._countDown, 1000);
            }
        }
    });

    return React.createClass({
        getInitialState: function () {
            return {
                message: Messages.messageToShowState.value,
                playerLive: MainPlayer.playerLiveState.value
            };
        },

        render: function () {
            return (
                <div id="interface">
                    <Profiling />
                    {this._middleBar()}
                    {this._middleTopBar()}
                    {this._cooldownBar()}
                    {this._serverMessage()}
                    {this._deathScreen()}
                </div>
            );
        },

        componentDidMount: function () {
            document.addEventListener('keydown', this._onKeyDown);
            document.addEventListener('mousedown', this._onMouseDown);
            document.onclick = this._onMouseDown;
            Messages.messageToShowState.subscribe(this._serverMessageUpdate);
            MainPlayer.playerLiveState.subscribe(this._playerLiveUpdate);
            Dispatcher.userEventStream.subscribe('join-battle-triggered', this._showBattleChooser);
        },

        componentWillUnmount: function () {
            document.removeEventListener('keydown', this._onKeyDown);
            document.removeEventListener('mousedown', this._onMouseDown);
            Messages.messageToShowState.unsubscribe(this._serverMessageUpdate);
            MainPlayer.playerLiveState.unsubscribe(this._playerLiveUpdate);
        },

        _middleBar: function () {
            if (this.state.playerLive) {
                return <MiddleBottomBar />;
            }
        },
        _middleTopBar: function () {
            if (this.state.playerLive) {
                return <MiddleTopBar />;
            }
        },
        _serverMessage: function () {
            if (this.state.message) {
                return <span id="server_message">{this.state.message}</span>;
            }
        },
        _cooldownBar: function () {
            if (this.state.playerLive) {
                return <CooldownBar />;
            }
        },
        _deathScreen: function () {
            if (!this.state.playerLive) {
                return <DeathScreen/>;
            }
        },

        _serverMessageUpdate: function (message) {
            const state = this.state;
            state.message = message;
            this.setState(state)
        },

        _playerLiveUpdate: function (live) {
            const state = this.state;
            state.playerLive = live;
            this.setState(state)
        },

        _onKeyDown: function (event) {
            Dispatcher.keyPressStream.publish(event.keyCode, null);
            Dispatcher.keyPressStream.publish(String.fromCharCode(event.keyCode).toUpperCase(), null);
            Dispatcher.keyPressStream.publish(String.fromCharCode(event.keyCode).toLowerCase(), null);
        },

        _onMouseDown: function (event) {
            if (event.which && event.which == 1) {
                Dispatcher.userEventStream.publish('left-click', event);
            }
            if (event.which && event.which == 3) {
                Dispatcher.userEventStream.publish('right-click', event);
            }
        }
    });
});