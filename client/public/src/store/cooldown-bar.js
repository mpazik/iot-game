define(function (require, exports, module) {
    const MessageIds = require('../common/packet/messages').ids;
    const SkillStore = require('./skill');
    const Dispatcher = require('../component/dispatcher');
    const MainPlayer = require('./main-player');
    const Publisher = require('../common/basic/publisher');

    var changePlayerReadiness = null;
    var timeOutToReady = null;

    Dispatcher.messageStream.subscribe(MessageIds.SkillUsed, function (event) {
        if (event.casterId != MainPlayer.characterId()) return;
        const skill = SkillStore.skill(event.skillId);
        changePlayerReadiness({cooldown: skill.cooldown});
        clearTimeout(timeOutToReady);
        timeOutToReady = setTimeout(() => changePlayerReadiness(null), skill.cooldown)
    });

    Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, function (event) {
        if (event.characterId != MainPlayer.characterId()) return;
        clearTimeout(timeOutToReady);
        changePlayerReadiness(null);
    });

    module.exports = {
        playerCooldown: new Publisher.StatePublisher(null, (push) => changePlayerReadiness = push)
    };
});