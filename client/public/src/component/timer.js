define(function (require, exports, module) {
    const NetworkDispatcher = require('./network-dispatcher');

    var pingScheduler;
    var offset = 0;
    var ping = 0;

    module.exports = {
        runOnServerTime(time, duration, func) {
            const timeToEvent = time - offset - Date.now();
            if (timeToEvent < 0)
                func(duration - timeToEvent);
            else
                setTimeout(() => func(duration), timeToEvent)
        },
        currentTimeOnServer() {
            return Date.now() + offset;
        },
        connect() {
            const socket = NetworkDispatcher.newSocket('time');
            socket.onMessage = (message) => {
                const i = message.indexOf(' ');
                const clientTime = parseInt(message.slice(0, i).toLowerCase());
                const serverTime = parseInt(message.slice(i + 1));
                var oneWayPing = (Date.now() - clientTime) / 2;
                offset = serverTime - Date.now() + oneWayPing;
                ping = Date.now() - clientTime;
            };
            socket.onOpen = () => {
                const ping = () => {
                    socket.send(Date.now())
                };
                ping();
                pingScheduler = setInterval(ping, 2000);
            };
            socket.onClose = () => {
                clearInterval(pingScheduler);
            };
        },
        get ping() {
            return ping
        }
    };
});