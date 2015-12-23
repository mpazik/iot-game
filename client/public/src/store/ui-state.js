define(function (require, exports, module) {
    const MainPlayerStore = require('./main-player');
    const ScenarioStore = require('./scenario');
    const ActionBar = require('./action-bar');
    const Publisher = require('../common/basic/publisher');
    const Application = require('../component/application');

    module.exports = {
        playerAlive: MainPlayerStore.playerLiveState,
        playerRespawnTimeState: MainPlayerStore.playerRespawnTimeState,
        playerData: MainPlayerStore.playerData,
        scenarioType: new Publisher.StatePublisher(null, (push) => {
            ScenarioStore.data.subscribe(scenario => {
               push(scenario.type);
            });
        }),
        endScenario: new Publisher.StatePublisher(false, (push) => {
            ScenarioStore.endScenarioData.subscribe(scenario => {
                push(!!scenario);
            });
        }),
        endScenarioData: ScenarioStore.endScenarioData,
        applicationState: Application.state,
        actionBarSkills: ActionBar.skills,
        actionBarActiveSkill: ActionBar.activeState
    };
});