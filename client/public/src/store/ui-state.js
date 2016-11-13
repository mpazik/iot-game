define(function (require, exports, module) {
    const MainPlayerStore = require('./main-player');
    const ScenarioStore = require('./scenario');
    const ProfilingStore = require('./profiling');
    const ServerMessagesStore = require('./server-messages');
    const Publisher = require('../common/basic/publisher');
    const Application = require('../component/application');
    const Instance = require('../component/instance/instance-controller');
    const Item = require('./item');
    const Chat = require('../component/chat');
    const Friends = require('../component/friends');
    const Cursor = require('../store/cursor');
    const Actions = require('../store/actions');
    const Quest = require('../store/quest');
    const Parcel = require('../component/parcel');

    module.exports = {
        playerAlive: MainPlayerStore.playerLiveState,
        playerRespawnTimeState: MainPlayerStore.playerRespawnTimeState,
        userNick: MainPlayerStore.userNick,
        userId: MainPlayerStore.userId,
        scenario: ScenarioStore.data,
        scenarioType: new Publisher.StatePublisher(null, (push) => {
            ScenarioStore.data.subscribe(scenario => {
                if (scenario == null) {
                    push(null);
                } else {
                    push(scenario.type);
                }
            });
        }),
        scenarioResolution: new Publisher.StatePublisher(null, (push) => {
            ScenarioStore.endScenarioData.subscribe(endScenario => {
                if (endScenario == null) {
                    push(null);
                } else {
                    push(endScenario.resolution.toLowerCase());
                }
            });
        }),
        endScenario: ScenarioStore.endScenarioData,
        applicationState: Application.state,
        instanceState: Instance.state,
        profilingStats: ProfilingStore.updateStatsState,
        cooldown: MainPlayerStore.playerCooldown,
        gameMessage: ServerMessagesStore.messageToShowState,
        serverError: ServerMessagesStore.errorToShowState,
        chatState: Chat.state,
        playerItems: Item.itemsChange,
        friendsConnectionState: Friends.connectionStatePublisher,
        friendshipRequest: Friends.friendshipRequestPublisher,
        customCursor: Cursor.cursorType,
        casting: Actions.playerCasting,
        questToDisplay: Quest.questToDisplay,
        completeQuestToDisplay: Quest.completeQuestToDisplay,
        activeQuests: Quest.activeQuests,
        isPlayerOnOwnParcel: Parcel.isPlayerOnOwnParcel
    };
});