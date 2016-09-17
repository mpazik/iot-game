define(function (require) {
    const userEventStream = require('../../component/dispatcher').userEventStream;
    
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
    <div class="form-group">
    <label>Scenario</label>
        <select>
            <option value="small-island">Small Island - Survival</option>
        </select>
    </div>
    <div class="form-group">
        <label>Difficulty:</label>
        <input type="number" class="difficulty-level" required="required">
    </div>
    <div class="form-group">
    <input type="submit" value="Join!">
    </div>
</form>
`;
        },
        attached: function () {
            const inputs = Array.prototype.slice.call(this.getElementsByTagName('input'));
            inputs.forEach((input) => input.addEventListener('keydown', (event) => {
                if (event.keyCode != KEY_CODES.ESC) {
                    event.stopPropagation();
                }
            }));

            function getLastDifficultyLevel() {
                const storageItem = localStorage.getItem('lastDifficultyLevel');
                return storageItem == null ? 1 : storageItem;
            }

            const lastDifficultyLevel = getLastDifficultyLevel();
            const difficultyLevelInput = this.getElementsByClassName('difficulty-level')[0];
            const select = this.getElementsByTagName('select')[0];
            const form = this.getElementsByTagName('form')[0];
            const formSubmitButton = this.querySelector('input[type=submit]');
            difficultyLevelInput.value = lastDifficultyLevel;
            difficultyLevelInput.min = 1;
            difficultyLevelInput.max = lastDifficultyLevel + 5;

            deffer(function () {
                formSubmitButton.focus()
            });
            form.onsubmit = function () {
                const map = select.value;
                const difficultyLevel = difficultyLevelInput.value;
                userEventStream.publish('join-battle', {map, difficultyLevel});
                return false;
            }.bind(this)
        }
    });
});