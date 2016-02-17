define(function (require, exports, module) {
    const Dispatcher = require('../component/dispatcher');
    var ResponseIds = require('../common/packet/messages').ids;
    const MessageIds = require('../common/packet/messages').ids;

    var offset = 0;

    Dispatcher.messageStream.subscribe(ResponseIds.TimeSync, (response) => {
        var oneWayPing = (Date.now() - response.clientTime) / 2;
        offset = response.serverTime - Date.now() + oneWayPing;
    });

    Dispatcher.messageStream.subscribe(MessageIds.InitialData, function (response) {
        offset = response.serverTime - Date.now();
    });

    function runOnServerTime(time, duration, func) {
        const timeToEvent = time - offset - Date.now();
        if (timeToEvent < 0)
            func(duration - timeToEvent);
        else
            setTimeout(() => func(duration), timeToEvent)
    }

    function currentTimeOnServer() {
        return Date.now() + offset;
    }

    module.exports = {
        runOnServerTime: runOnServerTime,
        currentTimeOnServer: currentTimeOnServer
    };
});