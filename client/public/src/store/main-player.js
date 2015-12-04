define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    var playerId = null;
    var characterId = null;
    var setPlayerLiveState = null;
    Dispatcher.messageStream.subscribe(MessageIds.InitialData, function (response) {
        playerId = response.playerId;
        characterId = response.characterId;
        setPlayerLiveState(true)
    });

    Dispatcher.messageStream.subscribe(MessageIds.CharacterSpawned, (event) => {
        if (event.character.id == characterId)
            setPlayerLiveState(true)
    });

    Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, (event) => {
        if (event.characterId == characterId)
            setPlayerLiveState(false)
    });

    module.exports = {
        // positions are recalculated in character renderer
        position: new Point(0, 0),
        positionInPixels: new Point(0,0),
        playerId: () => playerId,
        characterId: () => characterId,
        playerLiveState: new Publisher.StatePublisher(true, (push) => setPlayerLiveState = push)
    };
});