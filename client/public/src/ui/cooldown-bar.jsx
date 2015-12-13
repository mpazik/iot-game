define(function (require) {
    const React = require('lib/react');
    const CooldownBar = require('../store/cooldown-bar');

    return React.createClass({
        getInitialState: function () {
            return {display: CooldownBar.playerCooldown.value ? true : false};
        },

        componentDidMount: function () {
            CooldownBar.playerCooldown.subscribe(this._updateCooldown);
        },

        componentWillUnmount: function () {
            CooldownBar.playerCooldown.unsubscribe(this._updateCooldown);
        },

        render: function () {
            return (
                <div id="centerBottom">
                    {this._progressBar()}
                </div>
            );
        },

        _progressBar: function () {
            if (!this.state.display) return;
            var divStyle = {
                animationDuration: this.state.cooldown + 'ms'
            };
            return <div id="progressBar">
                <div className="bar_progress" style={divStyle}></div>
            </div>;
        },

        _updateCooldown: function (state) {
            if (state) {
                this.setState({display: true, cooldown: state.cooldown});
            }else {
                this.setState({display: false});
            }
        }
    });
});