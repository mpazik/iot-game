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
    const UserService = require('./user-service');
    const Timer = require('./timer');

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

    function connectToArbiter(userToken) {
        arbiterSocket = NetworkDispatcher.newSocket('arbiter', userToken);
        arbiterSocket.onMessage = (data) => {
            const message = arbiterProtocol.parse(data);
            if (message.constructor === JoinToInstance) {
                InstanceController.connect(message.instanceKey, userToken);
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

            // connection could be closed due to invalid user token.
            UserService.clearUserToken();
        };
    }

    function connect() {
        InstanceController.readyToConnect();
        if (UserService.userToken == null) {
            UserService.reissueUserToken()
                .then(connect)
                .catch(() => {
                    setState('need-authentication');
                });
        } else {
            connectToArbiter(UserService.userToken);
            Chat.connect(UserService.userToken);
            Timer.connect();
            setState('connecting');
        }
    }

    function loadGameAssets() {
        setState('loading-game-assets');

        ResourcesStore.load().then(connect);
    }

    module.exports = {
        state: statePublisher,
        init (gameElement) {
            loadGameAssets();
            InstanceController.init(gameElement)
        },
        connect,
        logout () {
            UserService.logout();
            NetworkDispatcher.disconnect();
        },
        retrievingPassword () {
            setState('retrieving-password')
        }
    };
});