define(function (require, exports, module) {
    const Publisher = require('../../common/basic/publisher');
    const Dispatcher = require('./../dispatcher');
    const NetworkDispatcher = require('./../network-dispatcher');
    const Commands = require('./commands');
    const Messages = require('./messages');
    const JsonProtocol = require('../../common/basic/json-protocol');

    const instanceProtocol = new JsonProtocol(Messages, Commands);

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

    Network.State = State;
    Network.prototype.connect = function (instanceKey, userToken) {
        var _this = this;
        var socket = NetworkDispatcher.newSocket(instanceKey, userToken);
        this.updateState(State.CONNECTING);
        socket.onMessage = function (data) {
            const message = instanceProtocol.parse(data);
            Dispatcher.messageStream.publish(message.constructor, message);
        };
        socket.onClose = function () {
            _this.updateState(State.DISCONNECTED);
        };
        socket.onOpen = function () {
            _this.updateState(State.CONNECTED);
        };
        socket.onError = function (error) {
            if (_this.state.value === State.CONNECTING) {
                console.error(error);
            }
        };
        this.socket = socket;
    };
    Network.prototype.sendCommand = function (command) {
        this.socket.send(instanceProtocol.serialize(command));
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