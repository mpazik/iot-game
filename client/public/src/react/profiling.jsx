define(function (require) {
    const StatsStore = require('../store/stats');
    const React = require('lib/react');

    return React.createClass({
        getInitialState: function () {
            return StatsStore.updateStatsState.value;
        },
        componentDidMount: function () {
            StatsStore.updateStatsState.subscribe(this._update)
        },
        componentWillUnmount: function () {
            StatsStore.updateStatsState.unsubscribe(this._update)
        },
        render: function () {
            return (
                <div id="profiling">
                    <div id="profiling_fps">FPS - {this.state.fps}</div>
                    <div id="profiling_rendering_time">RFT - {roundToDisplay(this.state.rft)}</div>
                    <div id="profiling_ping">PING - {this.state.ping}</div>
                    <div id="position_x">x: {roundToDisplay(this.state.position.x)}</div>
                    <div id="position_y">y: {roundToDisplay(this.state.position.y)}</div>
                </div>
            );
        },

        _update: function (stats) {
            this.setState(stats);
        }
    });

    function roundToDisplay(number) {
        return Math.round(number * 1000) / 1000;
    }

});