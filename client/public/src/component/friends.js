define((require, exports, module) => {
    const Publisher = require('../common/basic/publisher');
    const NetworkDispatcher = require('./network-dispatcher');
    const JsonProtocol = require('../common/basic/json-protocol');
    const Dispatcher = require('./dispatcher');
    const CharacterStore = require('../store/character');
    const Message = require('../store/server-messages');
    const SkillsIds = require('../common/model/skills').Ids;
    const MainPlayer = require('../store/main-player');

    const ClientMessage = {
        RequestForFriendShip: function (userId) {
            this.userId = userId;
        },
        AcceptRequest: function (userId) {
            this.userId = userId;
        }
    };
    const ServerMessage = {
        FriendshipRequest: function (userId, nick) {
            this.userId = userId;
            this.nick = nick;
        },
        FriendshipEstablished: function (userId, nick) {
            this.userId = userId;
            this.nick = nick;
        }
    };

    const protocol = new JsonProtocol(ServerMessage, ClientMessage);

    const friends = new Map();

    var socket = null;
    var publishFriendship = null;
    const friendshipPublisher = new Publisher.StreamPublisher(function (f) {
        return publishFriendship = f;
    });

    var publishFriendshipRequest = null;
    const friendshipRequestPublisher = new Publisher.StatePublisher(null, function (f) {
        return publishFriendshipRequest = f;
    });

    var setConnectionState = null;
    const connectionStatePublisher = new Publisher.StatePublisher('not-connected', function (f) {
        return setConnectionState = f;
    });

    var displayFriendshipMessage = false;

    function handleMessage(message) {
        switch (message.constructor) {
            case ServerMessage.FriendshipEstablished:
                friends.set(message.userId, message.nick);
                publishFriendship(message);
                if (displayFriendshipMessage) {
                    Message.displayMessage(`Player "${message.nick}" become your friend`);
                    displayFriendshipMessage = false;
                }
                break;
            case ServerMessage.FriendshipRequest:
                publishFriendshipRequest(message);
                displayFriendshipMessage = true;
                break;
        }
    }

    Dispatcher.userEventStream.subscribe('special-skill-used-on-character', (event) => {
        if (event.skillId != SkillsIds.INTRODUCE) {
            return;
        }
        if (event.userId == MainPlayer.userId()) {
            Message.displayMessage('You can not introduce to your self');
        }
        if (friends.has(event.userId)) {
            Message.displayMessage(`Are are already friend with ${friends.get(event.userId)}`);
        }
        const character = CharacterStore.character(event.characterId);
        const userId = character.userId;
        send(new ClientMessage.RequestForFriendShip(userId));
        Message.displayMessage('Request to player has been sent');
    });

    Dispatcher.userEventStream.subscribe('accept-friendship-request', (event) => {
        send(new ClientMessage.AcceptRequest(event.userId));
        displayFriendshipMessage = true;
        publishFriendshipRequest(null);
    });

    Dispatcher.userEventStream.subscribe('reject-friendship-request', () => {
        displayFriendshipMessage = false;
        publishFriendshipRequest(null);
    });

    function send(message) {
        socket.send(protocol.serialize(message));
    }

    module.exports = {
        friends,
        friendshipPublisher: friendshipPublisher,
        friendshipRequestPublisher: friendshipRequestPublisher,
        connectionStatePublisher: connectionStatePublisher,
        connect(userToken) {
            socket = NetworkDispatcher.newSocket('friends', userToken);
            socket.onMessage = (data) => {
                const message = protocol.parse(data);
                handleMessage(message)
            };
            socket.onOpen = () => {
                setConnectionState('connected')
            };
            socket.onClose = () => {
                setConnectionState('disconnected')
            };
        }
    }
});
