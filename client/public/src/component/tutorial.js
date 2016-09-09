define((require, exports, module) => {
    const Resources = require('../store/resources');
    const Achievements = require('./achievement');
    const Publisher = require('../common/basic/publisher');
    const UserEventStream = require('./dispatcher').userEventStream;
    const Timer = require('./timer');

    module.exports = {
        toDisplay: new Publisher.StatePublisher(null, (callback) => {
            Achievements.achievementChangePublisher.subscribe(Achievements.Changes.AchievementUnlocked, (change) => {
                const achievementUnlockedKey = change.key;
                const tutorialToDisplay = Resources.tutorials.find(tutorial => {
                    return tutorial['trigger']['type'] = 'achievement-unlocked' &&
                        tutorial['trigger']['properties']['achievement-key'] == achievementUnlockedKey;
                });
                if (!tutorialToDisplay) {
                    return;
                }

                const achievementToTrack = tutorialToDisplay['track-achievement'];
                if (achievementToTrack) {
                    Achievements.trackAchievement(achievementToTrack['achievement-key']);
                }

                const unlockTime = Achievements.state.achievementsUnlocked.get(achievementUnlockedKey);
                // Achievement unlocked within last minute. We want display tutorial again in case that page was refreshed.
                if (unlockTime > Timer.currentTimeOnServer() - 5000) {
                    callback(tutorialToDisplay)
                }

            });
            UserEventStream.subscribe('closed-tutorial-window', () => {
                callback(null)
            })
        })
    }
});
