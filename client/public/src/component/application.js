/**
 * Starting the game
 * ----------------------------
 *  1. Loading web resources (HTML, CSS, JS).
 *  2. Loading game assets (textures, sounds) and getting address of the server to connect
 *  3. Verification of the user.
 *  4. Connecting to the arbiter and chat.
 *  5. Waiting for a message from arbiter with instance key.
 *  6. Connecting to the instance server.
 */

define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('./dispatcher');
    const NetworkDispatcher = require('./network-dispatcher');
    const JsonProtocol = require('../common/basic/json-protocol');
    const InstanceController = require('./instance/instance-controller');
    const ResourcesStore = require('../store/resources');
    const Chat = require('./chat');
    var userNick;

    function JoinToInstance(instanceKey) {
        this.instanceKey = instanceKey;
    }

    function JoinToBattleCommand(map, difficultyLevel) {
        this.map = map;
        this.difficultyLevel = difficultyLevel;
    }

    function GoHomeCommand() {
    }

    const arbiterProtocol = JsonProtocol.Builder()
        .registerParsingMessageType(1, JoinToInstance)
        .registerSerializationMessageType(1, JoinToBattleCommand)
        .registerSerializationMessageType(2, GoHomeCommand)
        .build();

    var arbiterSocket = null;

    function sendJoinBattle(data) {
        arbiterSocket.send(arbiterProtocol.serialize(new JoinToBattleCommand(data.map, data.difficultyLevel)));
    }

    function sendGoToHome() {
        arbiterSocket.send(arbiterProtocol.serialize(new GoHomeCommand()));
    }

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('started', function (f) {
        return setState = f;
    });

    function getCookie(cookieName) {
        var name = cookieName + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1);
            if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
        }
        return null;
    }

    function deleteCookie(name) {
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }

    function getAndCheckUserNick() {
        return new Promise((resolve, reject) => {
            const nick = getCookie('nick');
            if (nick == null) {
                return reject()
            }
            Request.Server.canPlayerLogin(nick).then(function () {
                resolve(nick)
            }).catch(function (error) {
                reject(error);
            });
        });
    }

    function connectToArbiter(userNick) {
        arbiterSocket = NetworkDispatcher.newSocket('arbiter', userNick);
        arbiterSocket.onMessage = (data) => {
            const message = arbiterProtocol.parse(data);
            if (message.constructor === JoinToInstance) {
                InstanceController.connect(message.instanceKey, userNick);
                Chat.joinToInstanceChannel(message.instanceKey);
            }
        };
        arbiterSocket.onOpen = () => {
            Dispatcher.userEventStream.subscribe('join-battle', sendJoinBattle);
            Dispatcher.userEventStream.subscribe('go-to-home', sendGoToHome);
            setState('connected');
        };
        arbiterSocket.onClose = () => {
            Dispatcher.userEventStream.unsubscribe('join-battle', sendJoinBattle);
            Dispatcher.userEventStream.unsubscribe('go-to-home', sendGoToHome);
            setState('disconnected');
        };
    }

    function connect() {
        if (userNick == null) {
            getAndCheckUserNick().then(nick=> {
                userNick = nick;
                connect();
            }).catch(error => {
                if (error) {
                    console.error(error);
                }
                setState('need-authentication');
            });
        } else {
            connectToArbiter(userNick);
            Chat.connect(userNick);
            setState('connecting');
        }
    }

    function loadGameAssets() {
        setState("loading-game-assets");

        ResourcesStore.load().then(connect);
    }

    module.exports = {
        state: statePublisher,
        init: function (gameElement) {
            loadGameAssets();
            InstanceController.init(gameElement)
        },
        setUser: function (nick) {
            document.cookie = "nick=" + nick;
        },
        connect,
        logout: function () {
            userNick = null;
            deleteCookie('nick');
            NetworkDispatcher.disconnect();
        }
    };
});