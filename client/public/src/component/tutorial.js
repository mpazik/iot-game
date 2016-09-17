define((require, exports, module) => {
    const Resources = require('../store/resources');
    const Achievements = require('./achievement');
    const UserEventStream = require('./dispatcher').userEventStream;
    const Timer = require('./timer');
    const UiState = require('../store/ui-state');

    var tutorialToDisplay = {};
    var displayTutorialWindowWhenOnOpenWorldInstance = false;

    function displayTutorialWindow() {
        UserEventStream.publish('toggle-window', 'tutorial-window');
    }

    UiState.scenarioType.subscribe((instanceType) => {
        if (instanceType == 'open-world' && displayTutorialWindowWhenOnOpenWorldInstance) {
            displayTutorialWindowWhenOnOpenWorldInstance = false;
            displayTutorialWindow();
        }
    });

    Achievements.achievementChangePublisher.subscribe(Achievements.Changes.AchievementUnlocked, (change) => {
        const achievementUnlockedKey = change.key;
        const tutorial = Resources.tutorials.find(tutorial => {
            return tutorial['trigger']['type'] = 'achievement-unlocked' &&
                tutorial['trigger']['properties']['achievement-key'] == achievementUnlockedKey;
        });
        if (!tutorial) {
            return;
        }
        tutorialToDisplay = tutorial;

        const achievementToTrack = tutorial['track-achievement'];
        if (achievementToTrack) {
            Achievements.trackAchievement(achievementToTrack['achievement-key']);
        }

        const unlockTime = Achievements.state.achievementsUnlocked.get(achievementUnlockedKey);
        // Achievement unlocked within last minute. We want display tutorial again in case that page was refreshed.
        if (unlockTime > Timer.currentTimeOnServer() - 5000) {
            if (UiState.scenarioType.value == 'open-world') {
                setTimeout(displayTutorialWindow, 1000);
            } else {
                displayTutorialWindowWhenOnOpenWorldInstance = true;
            }
        }

    });

    module.exports = {
        get tutorialToDisplay() {
            return tutorialToDisplay;
        }
    }
});
