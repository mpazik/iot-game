define(function (require, exports, module) {
    const MainPlayerStore = require('./main-player');
    const ScenarioStore = require('./scenario');
    const Publisher = require('../common/basic/publisher');

    module.exports = {
        playerAlive: MainPlayerStore.playerLiveState,
        playerRespawnTimeState: MainPlayerStore.playerRespawnTimeState,
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
        endScenarioData: ScenarioStore.endScenarioData
    };
});