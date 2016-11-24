define((require) => {
    const uiState = require('../../store/ui-state');

    function updateStats(stats) {
        const element = this;

        function setStat(property, value) {
            element.querySelector(`.${property}>span`).innerHTML = value.toFixed(2);
        }

        setStat('fps', stats.fps);
        setStat('ping', stats.ping);
        setStat('x', stats.position.x);
        setStat('y', stats.position.y);
    }

    return {
        key: 'profiling-display',
        type: 'fragment',
        requirements: {
            instanceState: Predicates.is('running')
        },
        template: `
<div class="fps">FPS: <span></span></div>
<div class="ping">PING: <span></span></div>
<div class="x">x: <span></span></div>
<div class="y">y: <span></span></div>
`,
        classes: ['area'],
        attached(element) {
            element.profilingUpdate = updateStats.bind(element);
            uiState.profilingStats.subscribeAndTrigger(element.profilingUpdate);
        },
        detached(element) {
            uiState.profilingStats.unsubscribe(element.profilingUpdate);
        }
    }
});