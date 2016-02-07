define(function (require, exports, module) {
    const MainPlayerStore = require('./main-player');
    const ScenarioStore = require('./scenario');
    const ActionBar = require('./action-bar');
    const ProfilingStore = require('./profiling');
    const ServerMessagesStore = require('./server-messages');
    const Publisher = require('../common/basic/publisher');
    const Application = require('../component/application');
    const Chat = require('../component/chat');

    module.exports = {
        playerAlive: MainPlayerStore.playerLiveState,
        playerRespawnTimeState: MainPlayerStore.playerRespawnTimeState,
        playerData: MainPlayerStore.playerData,
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
        actionBarSkills: ActionBar.skills,
        actionBarActiveSkill: ActionBar.activeState,
        profilingStats: ProfilingStore.updateStatsState,
        cooldown: MainPlayerStore.playerCooldown,
        gameMessage: ServerMessagesStore.messageToShowState,
        playerMessage: Chat.playerMessage
    };
});