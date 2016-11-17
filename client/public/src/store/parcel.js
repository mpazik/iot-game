define(function (require, exports, module) {
    const parcelSize = require('configuration').parcelSize;
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('./../component/dispatcher');
    const MainPlayerStore = require('./main-player');
    const Messages = require('../component/instance/messages');

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

    const isPlayerOnOwnParcel = new Publisher.StatePublisher(null, publish => {
        currentParcel.subscribe(parcel => publish(parcel == playerParcel))
    });

    var playerParcel = null;
    const parcels = [];

    Dispatcher.messageStream.subscribe(Messages.InitialData, (initialData) => {
        parcels.length = 0;
        initialData.state['parcel'].forEach(addParcel);
    });

    Dispatcher.messageStream.subscribe(Messages.ParcelClaimed, addParcel);

    function addParcel(parcel) {
        if (parcel.owner == MainPlayerStore.userId()) {
            playerParcel = parcel;
            Dispatcher.messageStream.publish('player-claimed-parcel', parcel)
        }
        parcels.push(parcel);
        checkCurrentParcel();
    }

    function getParcelForPosition(position) {
        return parcels.find((parcel) =>parcel.x == position.x && parcel.y == position.y)
    }

    function checkCurrentParcel() {
        const position = MainPlayerStore.position;
        const currentParcelPosition = {
            // -1 because to y is the top of the player and we want it middle
            x: Math.floor(position.x / parcelSize),
            y: Math.floor(position.y / parcelSize)
        };
        const lastParcel = currentParcel.value;
        if (!lastParcel) {
            setCurrentParcel({x: currentParcelPosition.x, y: currentParcelPosition.y, parcelName: 'No one\'s land'});
            return
        }
        const parcelForPosition = getParcelForPosition(currentParcelPosition);
        if (parcelForPosition) {
            if (lastParcel.x == currentParcelPosition.x &&
                lastParcel.y == currentParcelPosition.y &&
                lastParcel.parcelName == parcelForPosition.parcelName) {
                return;
            }
            setCurrentParcel(parcelForPosition);
        } else {
            if (lastParcel.x == currentParcelPosition.x &&
                lastParcel.y == currentParcelPosition.y &&
                lastParcel.parcelName == 'No one\'s land') {
                return;
            }
            setCurrentParcel({x: currentParcelPosition.x, y: currentParcelPosition.y, parcelName: 'No one\'s land'});
        }
    }

    module.exports = {
        currentParcel,
        currentParcelHighlighting,
        highlightCurrentParcel,
        playerParcelHighlighting,
        highlightPlayerParcel,
        isPlayerOnOwnParcel,
        get playerParcel() {
            return playerParcel;
        },
        checkCurrentParcel,
        canClaimCurrentLand () {
            return !playerParcel && !currentParcel.value.owner
        }
    };
});