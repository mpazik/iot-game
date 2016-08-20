define(function (require) {
    const uiState = require('../../store/ui-state');
    
    return createUiElement('profiling-display', {
        type: 'fragment',
        properties: {
            requirements: {
                instanceState: Predicates.is('running')
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
            const stats = uiState.profilingStats.value;
            this._updateStats(stats);
            uiState.profilingStats.subscribe(this._updateStats.bind(this));
            this.classList.add('area');
        },
        detached: function () {
            uiState.profilingStats.unsubscribe(this._updateStats.bind(this));
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