define(function (require) {
    var React = require('lib/react');
    var Application = require('../component/application');
    var Ui = require('jsx!./ui');

    var Started = React.createClass({
        render: function () {
            return (
                <h1 className="app_state">Started</h1>
            );
        }
    });

    var Connecting = React.createClass({
        render: function () {
            return (
                <h1 className="app_state">Connecting...</h1>
            );
        }
    });

    var Loading = React.createClass({
        render: function () {
            return (
                <h1 className="app_state">Loading...</h1>
            );
        }
    });

    var Running = React.createClass({
        render: function () {
            return (
                <div>
                    <Ui />
                    <div id="game"></div>
                </div>
            );
        }
    });

    var Disconnected = React.createClass({
        render: function () {
            return (
                <div>
                    <h1 className="app_state">Disconnected</h1>
                    <div className="center">
                        <button onClick={this._reconnect}>Reconnect</button>
                    </div>
                </div>
            );
        },
        _reconnect: function () {
            Application.reconnect();
        }
    });

    var Error = React.createClass({
        render: function () {
            return (
                <h1 className="app_state">Application Error</h1>
            );
        }
    });

    var states = {
        started: Started,
        connecting: Connecting,
        loading: Loading,
        running: Running,
        disconnected: Disconnected,
        error: Error
    };


    var App = React.createClass({
        getInitialState: function () {
            return {appState: Application.state.value};
        },
        componentDidMount: function () {
            Application.start();
            Application.state.subscribe(function (state) {
                this.setState({appState: state})
            }.bind(this))
        },
        render: function () {
            var State = states[this.state.appState];
            return (
                <State />
            );
        }
    });

    App.prototype.init = function () {
        React.render(
            <App />,
            document.getElementById('app')
        );
    };
    return App;

});