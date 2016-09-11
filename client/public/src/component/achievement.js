define((require, exports, module) => {
    const Publisher = require('../common/basic/publisher');
    const NetworkDispatcher = require('./network-dispatcher');
    const JsonProtocol = require('../common/basic/json-protocol');
    const ResourcesStore = require('../store/resources');
    const ActionBar = require('../store/action-bar');
    const Message = require('../store/server-messages');
    const Timer = require('./timer');

    const ClientMessage = {};
    const Changes = {
        AchievementProgressed: function (key, createdAt) {
            this.key = key;
            this.createdAt = createdAt;
        },
        AchievementUnlocked: function (key, createdAt) {
            this.key = key;
            this.createdAt = createdAt;
        }
    };

    const achievementProtocol = new JsonProtocol(Changes, ClientMessage);

    const state = {
        achievementsProgress: new Map(),
        achievementsUnlocked: new Map()
    };

    const trackedAchievements = [];

    var publishAchievementChange = null;
    const achievementChangePublisher = new Publisher.TypePublisher(function (f) {
        return publishAchievementChange = f;
    });

    var setConnectionState = null;
    const connectionStatePublisher = new Publisher.StatePublisher('not-connected', function (f) {
        return setConnectionState = f;
    });

    var setTrackedAchievements = null;
    const trackedAchievementPublisher = new Publisher.StatePublisher(trackedAchievements, function (f) {
        return setTrackedAchievements = f;
    });

    function updateState(change) {
        const achievementKey = change.key;
        switch (change.constructor) {
            case Changes.AchievementProgressed:
                const achievementProgress = state.achievementsProgress.get(achievementKey);
                if (achievementProgress == null) {
                    state.achievementsProgress.set(achievementKey, 1);
                } else {
                    state.achievementsProgress.set(achievementKey, achievementProgress + 1);
                }
                break;
            case Changes.AchievementUnlocked:
                state.achievementsUnlocked.set(achievementKey, change.createdAt);
                if (state.achievementsProgress.has(achievementKey)) {
                    state.achievementsProgress.delete(achievementKey)
                }
                trackedAchievements.remove(key => key == achievementKey);
                setTrackedAchievements(trackedAchievements.slice());
                const achievement = ResourcesStore.achievements.find(achievement => achievement.key == achievementKey);
                const rewards = achievement['rewards'];
                if (rewards) {
                    rewards.forEach(reward => {
                        if (reward['type'] == 'skill') {
                            ActionBar.addSkill(reward['skillKey'])
                        }
                    });
                }
                const justUnlocked = change.createdAt > Timer.currentTimeOnServer() - 1000;
                if (justUnlocked) {
                    Message.displayMessage(`Achievement "${achievementByKey(achievementKey).title}" unlocked`);
                }
                break;
        }
        publishAchievementChange(change.constructor, change)
    }

    function achievementByKey(key) {
        return ResourcesStore.achievements.find(a => a.key == key)
    }

    module.exports = {
        Changes,
        state,
        achievementChangePublisher: achievementChangePublisher,
        connectionStatePublisher: connectionStatePublisher,
        trackedAchievementPublisher: trackedAchievementPublisher,
        trackAchievement(achievementKey) {
            trackedAchievements.push(achievementKey);
            // slice used to pass array by value
            setTrackedAchievements(trackedAchievements.slice())
        },
        achievement: achievementByKey,
        get achievementList() {
            return ResourcesStore.achievements
        },
        connect(userToken) {
            const socket = NetworkDispatcher.newSocket('achievement', userToken);
            socket.onMessage = (data) => {
                const message = achievementProtocol.parse(data);
                updateState(message)
            };
            socket.onOpen = () => {
                setConnectionState('connected')
            };
            socket.onClose = () => {
                setConnectionState('disconnected')
            };
        }
    }
});
