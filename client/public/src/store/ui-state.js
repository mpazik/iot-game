define(function (require, exports, module) {
    const MainPlayerStore = require('./main-player');

    module.exports = {
        playerAlive: MainPlayerStore.playerLiveState,
        playerRespawnTimeState: MainPlayerStore.playerRespawnTimeState
    };
});