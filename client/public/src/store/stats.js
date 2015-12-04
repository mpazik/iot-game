define(function (require, exports, module) {
    var Publisher = require('../common/basic/publisher');
    var MainLoop = require('./main-loop');
    var MainPlayer = require('./main-player');
    var publishStats = null;
    var updateStatsState = new Publisher.StatePublisher({
        fps: 0,
        rft: 0,
        ping: 0,
        position: {x: 0, y: 0}
    }, function (fn) {
        return publishStats = fn;
    });

    MainLoop.updateStatsStream.subscribe(function (loopStats) {
        var rft = Math.round(loopStats.renderingFrameTime * 1000) / 1000;
        publishStats({fps: loopStats.fps, rft: rft, ping: 0, position: MainPlayer.position});
    });

    module.exports = {
        updateStatsState
    };
});