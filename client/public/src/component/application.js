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
    const Configuration = require('configuration');
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
    const Friends = require('./friends');
    const Analytics = require('./analytics');
    const QuestStore = require('../store/quest');

    const ClientMessage = {
        Travel: function (location) {
            this.location = location;
        }
    };

    const ServerMessage = {
        JoinToInstance: function (instanceKey) {
            this.instanceKey = instanceKey;
        }
    };

    const arbiterProtocol = new JsonProtocol(ServerMessage, ClientMessage);

    var arbiterSocket = null;

    function travel(objectKey) {
        const location = (() => {
            switch (objectKey) {
                case 'cave-entrance':
                    return 'cave';
                case 'ladder':
                    return 'archipelago';
            }
        })();
        arbiterSocket.send(arbiterProtocol.serialize(new ClientMessage.Travel(location)));
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
            Dispatcher.userEventStream.subscribe('travel-to-location', travel);
            setState('connected');
        };
        arbiterSocket.onClose = () => {
            Dispatcher.userEventStream.unsubscribe('travel-to-location', travel);
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

    function connectSubSystems(userToken) {
        connectToArbiter(userToken);
        Analytics.connect(userToken);
        Chat.connect(userToken);
        Timer.connect();
        Friends.connect(userToken);
        setState('connecting');
    }

    function connect() {
        QuestStore.init();
        InstanceController.readyToConnect();
        const userToken = UserService.userToken;
        if (userToken == null) {
            UserService.tryLoginUsingClientData()
                .catch(goToAuthenticationPage)
                .then(() => {
                    const userToken = UserService.userToken;
                    if (userToken != null) {
                        connectSubSystems(userToken);
                    } else {
                        goToAuthenticationPage();
                    }
                })
                .catch(console.error)
        } else {
            connectSubSystems(userToken)
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