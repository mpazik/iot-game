define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../component/instance/messages');
    const SkillStore = require('./skill');

    var characterId = null;
    var userId = null;
    var userNick = null;
    var timeOutToResetCooldown = null;

    Dispatcher.messageStream.subscribe(Messages.UserCharacter, (data) => {
        userId = data.userId;
        characterId = data.characterId;
        userNick = data.userNick;
    });

    module.exports = {
        // positions are recalculated in character renderer
        position: new Point(0, 0),
        positionInPixels: new Point(0, 0),
        characterId: () => characterId,
        userId: () => userId,
        userNick: () => userNick,
        playerLiveState: new Publisher.StatePublisher(false, (push) => {
            Dispatcher.messageStream.subscribe(Messages.InitialData, () => {
                push(true)
            });

            Dispatcher.messageStream.subscribe(Messages.CharacterSpawned, (event) => {
                if (event.character.id != characterId) return;
                push(true)
            });

            Dispatcher.messageStream.subscribe(Messages.CharacterDied, (event) => {
                if (event.characterId != characterId) return;
                push(false)
            });
            Dispatcher.messageStream.subscribe(Messages.Disconnected, () => {
                push(false)
            });
        }),
        playerRespawnTimeState: new Publisher.StatePublisher(null, (push) => {
            Dispatcher.messageStream.subscribe(Messages.CharacterSpawned, (event) => {
                if (event.character.id != characterId) return;
                push(null);
            });
        }),
        playerCooldown: new Publisher.StatePublisher(null, push => {
            Dispatcher.messageStream.subscribe(Messages.SkillUsedOnCharacter, checkAndSetCooldown);
            Dispatcher.messageStream.subscribe(Messages.SkillUsedOnWorldMap, checkAndSetCooldown);
            Dispatcher.messageStream.subscribe(Messages.SkillUsedOnWorldObject, checkAndSetCooldown);
            Dispatcher.messageStream.subscribe(Messages.SkillUsed, checkAndSetCooldown);

            Dispatcher.messageStream.subscribe(Messages.CharacterDied, function (event) {
                if (event.characterId != characterId) return;
                clearTimeout(timeOutToResetCooldown);
                push(null);
            });

            function checkAndSetCooldown(event) {
                if (event.casterId != characterId) return;
                const skill = SkillStore.skill(event.skillId);
                setCooldown(skill.cooldown);
            }

            function setCooldown(cooldown) {
                push({cooldown: cooldown});
                clearTimeout(timeOutToResetCooldown);
                timeOutToResetCooldown = setTimeout(() => push(null), cooldown)
            }
        })
    };
});