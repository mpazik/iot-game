define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const MessageIds = require('../common/packet/messages').ids;
    const SkillStore = require('./skill');

    var playerId = null;
    var characterId = null;
    var timeOutToResetCooldown = null;

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
                if (event.character.id != characterId) return;
                push(true)
            });

            Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, (event) => {
                if (event.characterId != characterId) return;
                push(false)
            });
            Dispatcher.messageStream.subscribe(MessageIds.Disconnected, () => {
                push(false)
            });
        }),
        playerRespawnTimeState: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(MessageIds.CharacterSpawned, (event) => {
                if (event.character.id != characterId) return;
                push(null);
            });
            Dispatcher.messageStream.subscribe(MessageIds.PlayerWillRespawn, (event) => {
                if (event.playerId != playerId) return;
                push(event.respawnTime);
            });
        }),
        playerCooldown: new Publisher.StatePublisher(null, push => {
            Dispatcher.messageStream.subscribe(MessageIds.SkillUsedOnCharacter, function (event) {
                if (event.casterId != characterId) return;
                const skill = SkillStore.skill(event.skillId);
                push({cooldown: skill.cooldown});
                clearTimeout(timeOutToResetCooldown);
                timeOutToResetCooldown = setTimeout(() => push(null), skill.cooldown)
            });

            Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, function (event) {
                if (event.characterId != characterId) return;
                clearTimeout(timeOutToResetCooldown);
                push(null);
            });
        })
    };
});