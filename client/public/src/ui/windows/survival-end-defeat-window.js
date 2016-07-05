define(function (require, exports, module) {
    const uiState = require('../../store/ui-state');
    const userEventStream = require('../../component/dispatcher').userEventStream;
    
    return createUiElement('survival-end-defeat-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            closeable: false,
            requirements: {
                endScenario: Predicates.isSet(),
                scenarioType: Predicates.is('survival'),
                scenarioResolution: Predicates.is('defeat')
            }
        },
        created: function () {
            this.innerHTML = `
<h1 class="defeat">Defeat</h1>
<p>Difficulty level: <span class="level"></span></p>
<div class="window-actions">
    <button class="large go-back">Go back</button>
    <button class="large try-again">Try again</button>
</div>
`;
        },
        attached: function () {
            this.classList.add('resolution-window');
            const scenario = uiState.scenario.value;
            const level = this.getElementsByClassName('level')[0];
            level.innerText = scenario.difficultyLevel;

            const goBackButton = this.getElementsByClassName('go-back')[0];
            goBackButton.onclick = function () {
                userEventStream.publish('go-to-home', {});
            }.bind(this);

            const tryAgainButton = this.getElementsByClassName('try-again')[0];
            deffer(function () {
                tryAgainButton.focus();
            });
            tryAgainButton.onclick = function () {
                userEventStream.publish('join-battle', {
                    map: scenario.mapName,
                    difficultyLevel: scenario.difficultyLevel
                });
            }.bind(this);
        }
    });
});