define(function (require) {
    const uiState = require('../../store/ui-state');
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const Leaderboard = require('../../component/leaderboard');

    return createUiElement('survival-end-victory-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            closeable: false,
            requirements: {
                endScenario: Predicates.isSet(),
                scenarioType: Predicates.is('survival'),
                scenarioResolution: Predicates.is('victory')
            }
        },
        created: function () {
            this.innerHTML = `
<h1 class="victory">Victory</h1>
<p>Difficulty level: <span class="level"></span></p>
<p>Your are: <span class="ranking">...</span></p>
<div class="window-actions">
    <button class="large go-back">Go back</button>
    <button class="large next-level">Next level</button>
</div>
`;
        },
        attached: function () {
            this.classList.add('resolution-window');
            const scenario = uiState.scenario.value;
            const level = this.getElementsByClassName('level')[0];
            level.innerText = scenario.difficultyLevel;
            localStorage.setItem('lastDifficultyLevel', scenario.difficultyLevel);

            const ranking = this.getElementsByClassName('ranking')[0];
            Leaderboard.userLeaderboardResult(uiState.userId()).then(function (data) {
                ranking.innerText = ordinalSuffixOf(data.position);
            });

            const goBackButton = this.getElementsByClassName('go-back')[0];
            goBackButton.onclick = function () {
                userEventStream.publish('go-to-home', {});
            }.bind(this);

            const nextLevelButton = this.getElementsByClassName('next-level')[0];
            deffer(function () {
                nextLevelButton.focus();
            });
            nextLevelButton.onclick = function () {
                userEventStream.publish('join-battle', {
                    map: scenario.mapName,
                    difficultyLevel: scenario.difficultyLevel + 1
                });
            }.bind(this);
        }
    });
});