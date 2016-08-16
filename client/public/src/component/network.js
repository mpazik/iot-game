define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const PacketSerialization = require('../common/packet/packet-serialization');
    const Dispatcher = require('./dispatcher');
    const NetworkDispatcher = require('./network-dispatcher');

    const State = {
        CREATED: 0,
        CONNECTING: 1,
        CONNECTED: 2,
        ERROR: 3,
        DISCONNECTING: 4,
        DISCONNECTED: 5
    };
    const stateToStr = ['CREATED', 'CONNECTING', 'CONNECTED', 'ERROR', 'DISCONNECTING', 'DISCONNECTED'];

    function Network() {
        var _this = this;
        this.publishState = null;
        this.state = new Publisher.StatePublisher(State.CREATED, function (f) {
            return _this.publishState = f;
        });
    }

    function parseJson(string) {
        try {
            return JSON.parse(string);
        } catch (e) {
            return undefined;
        }
    }

    Network.State = State;
    Network.prototype.connect = function (userNick) {
        var _this = this;
        var socket = NetworkDispatcher.newSocket('instance', userNick);
        this.updateState(State.CONNECTING);
        socket.onMessage = function (data) {
            const messages = parseJson(data);
            if (!messages) {
                console.error("Received wrong message from sever: " + data);
            } else {
                messages.forEach(function (message) {
                    return Dispatcher.messageStream.publish(message[0], message[1]);
                });
            }
        };
        socket.onClose = function () {
            _this.updateState(State.DISCONNECTED);
        };
        var self = this;
        var connectionPromise = new Promise(function (resolve, reject) {
            socket.onOpen = function () {
                //_this.sendRequests([new Requests.TimeSync(Date.now())]);
                self.updateState(State.CONNECTED);
                resolve();
            };
            socket.onError = function (error) {
                if (self.state.value === State.CONNECTING) {
                    reject(error);
                }
            };
        });
        this.socket = socket;
        return connectionPromise;
    };
    Network.prototype.sendCommands = function (commands) {
        var data = PacketSerialization.serialize(commands);
        this.socket.send(data);
    };
    Network.prototype.disconnect = function () {
        this.updateState(State.DISCONNECTING);
        this.socket.close();
    };

    Network.prototype.updateState = function (state) {
        console.log("Network: " + stateToStr[state]);
        this.publishState(state);
    };
    module.exports = Network;
});