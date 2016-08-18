define(function (require, exports, module) {
    const Publisher = require('../../common/basic/publisher');
    const Dispatcher = require('./../dispatcher');
    const NetworkDispatcher = require('./../network-dispatcher');
    const Commands = require('./commands');
    const Messages = require('./messages');

    const messageConstructorsForCode = (function () {
        const map = new Map();
        map.set(5, Messages.CharacterSpawned);
        map.set(6, Messages.CharacterDied);
        map.set(7, Messages.CharacterMoved);
        map.set(8, Messages.SkillUsedOnCharacter);
        map.set(9, Messages.CharacterGotDamage);
        map.set(11, Messages.InitialData);
        map.set(12, Messages.ServerMessage);
        map.set(16, Messages.TimeSync);
        map.set(17, Messages.JoinToInstance);
        map.set(19, Messages.ScenarioEnd);
        map.set(21, Messages.SkillUsedOnWorldMap);
        map.set(22, Messages.WorldObjectCreated);
        map.set(23, Messages.SkillUsedOnWorldObject);
        map.set(24, Messages.WorldObjectRemoved);
        return map;
    }());

    const commandCodesForConstructor = (function () {
        const map = new Map();
        map.set(Commands.Move, 2);
        map.set(Commands.UseSkillOnCharacter, 3);
        map.set(Commands.UseSkillOnWorldMap, 4);
        map.set(Commands.TimeSync, 6);
        map.set(Commands.JoinBattle, 7);
        map.set(Commands.Backdoor, 8);
        map.set(Commands.UseSkillOnWorldObject, 11);
        return map;
    }());

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
            const message = parseJson(data);
            const messageCode = message[0];
            const messageConstructor = messageConstructorsForCode.get(messageCode);
            if (!message) {
                console.error("Received wrong message from sever: " + data);
            } else {
                Dispatcher.messageStream.publish(messageConstructor, message[1]);
            }
        };
        socket.onClose = function () {
            _this.updateState(State.DISCONNECTED);
        };
        var self = this;
        var connectionPromise = new Promise(function (resolve, reject) {
            socket.onOpen = function () {
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
    Network.prototype.sendCommand = function (command) {
        const code = commandCodesForConstructor.get(command.constructor);
        this.socket.send(JSON.stringify([code, command]));
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