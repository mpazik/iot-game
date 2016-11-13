define(function (require, exports, module) {
    const NetworkDispatcher = require('./network-dispatcher');
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('./dispatcher');
    const MainPlayerStore = require('../store/main-player');
    const MainLoop = require('../store/main-loop');
    const JsonProtocol = require('../common/basic/json-protocol');
    const parcelSize = require('configuration').parcelSize;

    var socket = null;
    var setConnectionState = null;
    const connectionStatePublisher = new Publisher.StatePublisher('not-connected', function (f) {
        return setConnectionState = f;
    });

    var setCurrentParcel;
    const currentParcel = new Publisher.StatePublisher(null, function (f) {
        return setCurrentParcel = f;
    });

    var highlightCurrentParcel = null;
    const currentParcelHighlighting = new Publisher.StatePublisher(null, function (f) {
        return highlightCurrentParcel = f;
    });

    var highlightPlayerParcel = null;
    const playerParcelHighlighting = new Publisher.StatePublisher(null, function (f) {
        return highlightPlayerParcel = f;
    });

    const isPlayerOnOwnParcel = new Publisher.StatePublisher('not-connected', publish => {
        currentParcel.subscribe(parcel => publish(parcel == playerParcel))
    });

    var playerParcel = null;
    const parcels = [];

    function handleMessage(message) {
        switch (message.constructor) {
            case ServerMessage.ParcelClaimed:
                const parcel = message;
                if (parcel.owner == MainPlayerStore.userId()) {
                    playerParcel = parcel;
                    Dispatcher.messageStream.publish('player-claimed-parcel', parcel)
                }
                parcels.push(parcel);
                checkCurrentParcel();
                break;
        }
    }

    function getParcelForPosition(position) {
        return parcels.find((parcel) =>parcel.x == position.x && parcel.y == position.y)
    }

    function checkCurrentParcel() {
        const position = MainPlayerStore.position;
        const currentParcelPosition = {
            // -1 because to y is the top of the player and we want it middle
            x: Math.floor(position.x / parcelSize),
            y: Math.floor((position.y + 1) / parcelSize)
        };
        const lastParcel = currentParcel.value;
        if (!lastParcel) {
            setCurrentParcel({x: currentParcelPosition.x, y: currentParcelPosition.y, parcelName: 'No one\'s land'});
            return
        }
        if (lastParcel.x == currentParcelPosition.x &&
            lastParcel.y == currentParcelPosition.y &&
            lastParcel.parcelName == currentParcelPosition.parcelName) {
            return;
        }
        const parcelForPosition = getParcelForPosition(currentParcelPosition);
        if (parcelForPosition) {
            setCurrentParcel(parcelForPosition);
        } else {
            setCurrentParcel({x: currentParcelPosition.x, y: currentParcelPosition.y, parcelName: 'No one\'s land'});
        }
    }

    const ClientMessage = {
        ClaimParcel: function (x, y, owner, ownerName, parcelName) {
            this.x = x;
            this.y = y;
            this.owner = owner;
            this.ownerName = ownerName;
            this.parcelName = parcelName;
        }
    };

    const ServerMessage = {
        ParcelClaimed: function (x, y, owner, ownerName, parcelName) {
            this.x = x;
            this.y = y;
            this.owner = owner;
            this.ownerName = ownerName;
            this.parcelName = parcelName;
        }
    };

    const protocol = new JsonProtocol(ServerMessage, ClientMessage);

    module.exports = {
        claimLand (parcelName) {
            const data = protocol.serialize(new ClientMessage.ClaimParcel(currentParcel.value.x, currentParcel.value.y, MainPlayerStore.userId(), MainPlayerStore.userNick(), parcelName));
            socket.send(data);
        },
        connectionStatePublisher: connectionStatePublisher,
        connect (userToken) {
            //noinspection JSUnusedAssignment
            socket = NetworkDispatcher.newSocket('parcel', userToken);
            socket.onMessage = (data) => {
                const message = protocol.parse(data);
                handleMessage(message)
            };
            socket.onOpen = () => {
                setConnectionState('connected');
                MainLoop.renderStream.subscribe(checkCurrentParcel);
            };
            socket.onClose = () => {
                setConnectionState('disconnected');
                MainLoop.renderStream.unsubscribe(checkCurrentParcel);
            };
        },
        currentParcel,
        currentParcelHighlighting,
        highlightCurrentParcel,
        playerParcelHighlighting,
        highlightPlayerParcel,
        isPlayerOnOwnParcel,
        get playerParcel() {
            return playerParcel;
        },
        canClaimCurrentLand () {
            return !playerParcel && !currentParcel.value.owner
        }
    };
});