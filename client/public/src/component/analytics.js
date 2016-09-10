define((require, exports, module) => {
    const NetworkDispatcher = require('./network-dispatcher');
    const JsonProtocol = require('../common/basic/json-protocol');


    const ClientMessage = {
        AnalyticEvent: function (type) {
            this.type = type;
        },
        AnalyticDataEvent: function (type, data) {
            this.type = type;
            this.data = data;
        }
    };

    const protocol = new JsonProtocol({}, ClientMessage);

    var socket = null;

    module.exports = {
        sendEvent(type) {
            socket.send(protocol.serialize(new ClientMessage.AnalyticEvent(type)))
        },
        sendDataEvent(type, data) {
            socket.send(protocol.serialize(new ClientMessage.AnalyticDataEvent(type, data)))
        },
        connect(userToken) {
            socket = NetworkDispatcher.newSocket('analytics', userToken);
            socket.onMessage = (data) => {
            };
            socket.onOpen = () => {
            };
            socket.onClose = () => {
            };
        }
    }
});
