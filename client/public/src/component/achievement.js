define((require, exports, module) => {
    const Publisher = require('../common/basic/publisher');
    const NetworkDispatcher = require('./network-dispatcher');
    const JsonProtocol = require('../common/basic/json-protocol');
    const ResourcesStore = require('../store/resources');

    const ClientMessage = {};
    const ServerMessage = {
        AchievementProgressed: function (key) {
            this.key = key
        },
        AchievementUnlocked: function (key) {
            this.key = key
        }
    };

    var setConnectionState = null;
    const connectionStatePublisher = new Publisher.StatePublisher('not-connected', function (f) {
        return setConnectionState = f;
    });

    const achievementProtocol = new JsonProtocol(ServerMessage, ClientMessage);

    const state = {
        achievementsProgress: new Map(),
        achievementsUnlocked: new Set()
    };

    var updatedAchievementState = null;
    const achievementStatePublisher = new Publisher.StatePublisher(state, function (f) {
        return updatedAchievementState = f;
    });

    function updateState(change) {
        const achievementKey = change.key;
        switch (change.constructor) {
            case ServerMessage.AchievementProgressed:
                const achievementProgress = state.achievementsProgress.get(achievementKey);
                if (achievementProgress == null) {
                    state.achievementsProgress.set(achievementKey, 1);
                } else {
                    state.achievementsProgress.set(achievementKey, achievementProgress + 1);
                }
                break;
            case ServerMessage.AchievementUnlocked:
                state.achievementsUnlocked.add(achievementKey);
                if (state.achievementsProgress.has(achievementKey)) {
                    state.achievementsProgress.delete(achievementKey)
                }
                break;
        }
        updatedAchievementState(state)
    }

    module.exports = {
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
        },
        achievementState: achievementStatePublisher,
        connectionState: connectionStatePublisher,
        achievement: (key) => ResourcesStore.achievements.find(a => a.key == key),
        get achievementList() {
            return ResourcesStore.achievements
        }
    }
});
