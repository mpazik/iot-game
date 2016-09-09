define((require, exports, module) => {
    const Resources = require('../store/resources');
    const Achievements = require('./achievement');
    const Publisher = require('../common/basic/publisher');
    const UserEventStream = require('./dispatcher').userEventStream;

    module.exports = {
        toDisplay: new Publisher.StatePublisher(null, (callback) => {
            Achievements.achievementChangePublisher.subscribe(Achievements.Changes.AchievementUnlocked, (change) => {
                const tutorialToDisplay = Resources.tutorials.find(tutorial => {
                    return tutorial['trigger']['type'] = 'achievement-unlocked' &&
                        tutorial['trigger']['properties']['achievement-key'] == change.key;
                });
                if (tutorialToDisplay) {
                    const achievementToTrack = tutorialToDisplay['track-achievement'];
                    if (achievementToTrack) {
                        Achievements.trackAchievement(achievementToTrack['achievement-key'])
                    }
                    callback(tutorialToDisplay)
                }
            });
            UserEventStream.subscribe('closed-tutorial-window', () => {
                callback(null)
            })
        })
    }
});
