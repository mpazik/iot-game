define(function (require, exports, module) {
    const uiState = require('src/store/ui-state');
    
    return createUiElement('join-battle-window', {
        type: 'window',
        properties: {
            activateKeyBind: KEY_CODES.fromLetter('J'),
            requirements: {
                playerAlive: Predicates.is(true),
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            this.innerHTML = `
<form>
    <label>
        <select>
            <option value="small-island">Small Island - Survival</option>
        </select>
    </label>
    <label>
        Difficulty Level:
        <input type="number" class="difficulty-level" required="required">
    </label>
    <input type="submit" value="Join!">
</form>
`;
        },
        attached: function () {
            const playerData = uiState.playerData.value;
            const difficultyLevelInput = this.getElementsByClassName('difficulty-level')[0];
            const select = this.getElementsByTagName('select')[0];
            const form = this.getElementsByTagName('form')[0];
            const formSubmitButton = this.querySelector('input[type=submit]');
            difficultyLevelInput.value = playerData.lastDifficultyLevel;
            difficultyLevelInput.min = 1;
            difficultyLevelInput.max = playerData.highestDifficultyLevel + 5;

            deffer(function () {
                formSubmitButton.focus()
            });
            form.onsubmit = function () {
                const map = select.value;
                const difficultyLevel = difficultyLevelInput.value;
                this.game.publishUiAction('join-battle', {map, difficultyLevel});
                return false;
            }.bind(this)
        }
    });
});