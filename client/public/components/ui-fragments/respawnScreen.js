define(function (require, exports, module) {
    function countDown(element, timeToRespawn) {
        if (timeToRespawn > 0) {
            setTimeout(function () {
                countDown(element, (timeToRespawn - 0.1).toFixed(1))
            }, 100);
        }
        element.innerText = timeToRespawn;
    }

    return createUiElement('respawn-screen', {
        type: 'fragment',
        properties: {
            requirements: {
                playerAlive: Predicates.is(false),
                scenarioType: Predicates.is('open-world'),
                applicationState: Predicates.is('running')
            }
        },
        created: function () {
            this.innerHTML = `
<style>
    respawn-screen {
        position: absolute;
        width: 100%;
        height: 90%;
        background: rgba(0, 0, 0, 0.2);
        text-align: center;
        padding-top: 10%;
        top: 0;
    }
</style>
<div>
    <h1>Your character died.</h1>
    <h3>They will respawn in <span class="time-to-respawn"></span></h3>
</div>`;
        },
        attached: function () {
            const respawnTime = this.uiState.playerRespawnTimeState.value;
            this.timeToRespawnElement = this.getElementsByClassName('time-to-respawn')[0];
            if (respawnTime) {
                this._updateRespawnTime(respawnTime);
            }
            this.uiState.playerRespawnTimeState.subscribe(this._updateRespawnTime)
        },
        detached: function () {
            this.uiState.playerRespawnTimeState.unsubscribe(this._updateRespawnTime)
        },
        _updateRespawnTime: function (respawnTime) {
            const respawnInMillis = respawnTime - this.game.timer.currentTimeOnServer();
            const respawnInSeconds = (Math.floor(respawnInMillis / 100) / 10).toFixed(1);
            countDown(this.timeToRespawnElement, respawnInSeconds);
        }
    });
});