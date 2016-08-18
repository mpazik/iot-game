define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MainLoop = require('./main-loop');
    const MainPlayer = require('./main-player');
    const Timer = require('../component/timer');


    const initState = {
        fps: 0,
        ping: 0,
        position: {x: 0, y: 0}
    };

    module.exports = {
        updateStatsState: new Publisher.StatePublisher(initState, (push) => {
            MainLoop.updateStatsStream.subscribe(function (loopStats) {
                push({fps: loopStats.fps, ping: Timer.ping(), position: MainPlayer.position});
            });
        })
    };
});