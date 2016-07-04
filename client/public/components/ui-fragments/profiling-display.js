define(function (require, exports, module) {
    createUiElement('profiling-display', {
        type: 'fragment',
        properties: {
            requirements: {
                applicationState: Predicates.is('running')
            }
        },
        created: function () {
            this.innerHTML =  `
<div class="fps">FPS - <span></span></div>
<div class="ping">PING - <span></span></div>
<div class="x">x - <span></span></div>
<div class="y">y - <span></span></div>
`;
        },
        attached: function () {
            const stats = this.uiState.profilingStats.value;
            this._updateStats(stats);
            this.uiState.profilingStats.subscribe(this._updateStats.bind(this))
            this.classList.add('area');
        },
        detached: function () {
            this.uiState.profilingStats.unsubscribe(this._updateStats.bind(this))
        },
        _updateStats: function (stats) {
            const element = this;

            function setStat(property, value) {
                element.querySelector(`.${property}>span`).innerHTML = value.toFixed(2);
            }

            setStat('fps', stats.fps);
            setStat('ping', stats.ping);
            setStat('x', stats.position.x);
            setStat('y', stats.position.y);
        }
    });
});