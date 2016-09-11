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
    const Messages = require('./instance/messages');
    const ResourcesStore = require('../store/resources');
    const Chat = require('./chat');
    const UserService = require('./user-service');
    const Timer = require('./timer');
    const Achievement = require('./achievement');
    const Friends = require('./friends');
    const Analytics = require('./analytics');

    const ClientMessage = {
        JoinBattleCommand: function (map, difficultyLevel) {
            this.map = map;
            this.difficultyLevel = difficultyLevel;
        },
        GoHomeCommand: function () {
        }
    };

    const ServerMessage = {
        JoinToInstance: function (instanceKey) {
            this.instanceKey = instanceKey;
        }
    };

    const arbiterProtocol = new JsonProtocol(ServerMessage, ClientMessage);

    var arbiterSocket = null;

    function sendJoinBattle(data) {
        arbiterSocket.send(arbiterProtocol.serialize(new ClientMessage.JoinBattleCommand(data.map, data.difficultyLevel)));
    }

    function sendGoToHome() {
        arbiterSocket.send(arbiterProtocol.serialize(new ClientMessage.GoHomeCommand()));
    }

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('connecting', function (f) {
        return setState = f;
    });

    function connectToArbiter(userToken) {
        arbiterSocket = NetworkDispatcher.newSocket('arbiter', userToken);
        arbiterSocket.onMessage = (data) => {
            const message = arbiterProtocol.parse(data);
            if (message.constructor === ServerMessage.JoinToInstance) {
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
        arbiterSocket.onError = (error) => {
            const serverError = new Messages.ServerError(error);
            Dispatcher.messageStream.publish(Messages.ServerError, serverError);
            setState('disconnected');
        }
    }

    function goToAuthenticationPage() {
        window.location.href = Configuration.authenticationUrl;
    }

    function connect() {
        InstanceController.readyToConnect();
        const userToken = UserService.userToken;
        if (userToken == null) {
            UserService.tryLoginUsingClientData()
                .catch(goToAuthenticationPage)
                .then(connect)
                .catch(console.error)
        } else {
            connectToArbiter(userToken);
            Analytics.connect(userToken);
            Chat.connect(userToken);
            Timer.connect();
            Achievement.connect(userToken);
            Friends.connect(userToken);
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
        connect
    };
});