define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;

    var playerId = null;
    var characterId = null;

    Dispatcher.messageStream.subscribe(MessageIds.InitialData, function (response) {
        playerId = response.playerId;
        characterId = response.characterId;
    });


    module.exports = {
        // positions are recalculated in character renderer
        position: new Point(0, 0),
        positionInPixels: new Point(0, 0),
        playerId: () => playerId,
        characterId: () => characterId,
        playerData: new Publisher.StatePublisher({}, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.InitialData, (data) => {
                push(data.playerData)
            });
        }),
        playerLiveState: new Publisher.StatePublisher(false, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.InitialData, () => {
                push(true)
            });

            Dispatcher.messageStream.subscribe(MessageIds.CharacterSpawned, (event) => {
                if (event.character.id == characterId)
                    push(true)
            });

            Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, (event) => {
                if (event.characterId == characterId)
                    push(false)
            });
        }),
        playerRespawnTimeState: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.CharacterSpawned, (event) => {
                if (event.character.id == characterId)
                    push(null);
            });
            Dispatcher.messageStream.subscribe(MessageIds.PlayerWillRespawn, (event) => {
                if (event.playerId == playerId) {
                    push(event.respawnTime);
                }
            });
        })
    };
});