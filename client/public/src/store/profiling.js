define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MainLoop = require('./main-loop');
    const MainPlayer = require('./main-player');
    const Application = require('../component/application');
    const Commands = require('../common/packet/commands').constructors;
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    var pingScheduler;

    const initState = {
        fps: 0,
        ping: 0,
        position: {x: 0, y: 0}
    };

    Application.state.subscribeState('running', () => {
        function ping() {
            Application.sendCommands([new Commands.TimeSync(Date.now())])
        }
        pingScheduler = setInterval(ping, 2000);
    });
    Application.state.subscribeState('disconnected', () => {
        console.log('disconnected ping');
        clearInterval(pingScheduler);
    });

    module.exports = {
        updateStatsState: new Publisher.StatePublisher(initState, (push) => {
            var ping = 0;
            MainLoop.updateStatsStream.subscribe(function (loopStats) {
                push({fps: loopStats.fps, ping: ping, position: MainPlayer.position});
            });
            Dispatcher.messageStream.subscribe(MessageIds.TimeSync, function (response) {
                ping = Date.now() - response.clientTime;
            });
        })
    };
});