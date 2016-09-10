define(function (require) {
    const Analytics = require('../../component/analytics');
    const userEventStream = require('../../component/dispatcher').userEventStream;

    function goodbye(event) {
        if (localStorage.getItem('feedback-sent') == "true") {
            return
        }

        Analytics.sendEvent("feedback.reminder.opened");
        userEventStream.publish('toggle-window', 'feedback-window');
        if (!event) event = window.event;
        event.cancelBubble = true;

        if (event.stopPropagation) {
            event.stopPropagation();
            event.preventDefault();
        }

        const message = 'Give me feedback before leaving, please :)';
        event.returnValue = message;
        return message;
    }

    window.onbeforeunload = goodbye;

    const defaultFormData = '{"gameScore":3,"graphicScore":3,"gameIdeaScore":3,' +
        '"coopBattleScore":3,"fightingScore":3,"craftingScore":3,"tradingScore":3,' +
        '"buildingScore":3,"foundAnyBug":false,"bugDescription":"","age":null,"gender":"","comment":""}';

    return createUiElement('feedback-window', {
        type: 'window',
        properties: {
            activateKeyBind: KEY_CODES.fromLetter('G'),
            requirements: {
                playerAlive: Predicates.is(true),
                scenarioType: Predicates.is('open-world')
            }
        },
        created: function () {
            this.innerHTML = `
<h2>Give feedback</h2>
<form>
    <h4>How do you like it?</h4>
    <div class="form-group">
        <label>Game:</label>
        1 <input type="range" id="score-game" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>Graphic:</label>
        1 <input type="range" id="score-graphic" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>Game idea:</label>
        1 <input type="range" id="score-game-idea" value="3" min="1" max="5"> 5
    </div>

    <h4>How do you like the graphic?</h4>
    <div class="form-group">
        <label>Score:</label>
        1 <input type="range" id="score-graphic" value="3" min="1" max="5"> 5
    </div>
    
    <h4>What would you like to see in the game?</h4>
    <div class="form-group">
        <label>Co-op battles:</label>
        1 <input type="range" id="score-co-op-battles" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>More fighting:</label>
        1 <input type="range" id="score-fighting" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>Crafting:</label>
        1 <input type="range" id="score-crafting" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>Trading:</label>
        1 <input type="range" id="score-trading" value="3" min="1" max="5"> 5
    </div>
    <div class="form-group">
        <label>Building:</label>
        1 <input type="range" id="score-building" value="3" min="1" max="5"> 5
    </div>
    
    <h4>Have you experienced any bug?</h4>
    <div class="form-group">
        <label>Yes</label><input type="checkbox" id="feedback-any-bug-checkbox">
    </div>
    <div class="form-group" id="feedback-bug-description-box">
        <label style="display:block">Description</label>
        <textarea id="feedback-bug-description"></textarea>
    </div>
    
    <h4>Personal info</h4>
    <div class="form-group">
        <label>Age</label>
        <input type="number" id="feedback-age" min="5" max="100"></div>
    </div>
    <div class="form-group">
        <label>Gender</label>
        <select id="feedback-gender">
            <option></option>
            <option>male</option>
            <option>female</option>
        </select>
    </div>
    
    <h4>Want to add something?</h4>
    <div class="form-group">
        <label style="display:block">Your comment</label>
        <textarea id="feedback-comment"></textarea>
    </div>
    <div class="form-group">
        <input class="large" type="submit" value="Send">
    </div>
</form>
`;
        },
        attached: function () {
            const textareas = Array.prototype.slice.call(this.getElementsByTagName('textarea'));
            textareas.forEach((textarea) => textarea.addEventListener('keydown', (event) => {
                event.stopPropagation();
            }));
            const bugDescriptionBox = document.getElementById('feedback-bug-description-box');
            bugDescriptionBox.style.display = 'none';
            document.getElementById('feedback-any-bug-checkbox').addEventListener('change', function () {
                bugDescriptionBox.style.display = this.checked ? 'block' : 'none'
            });

            const form = this.getElementsByTagName('form')[0];
            form.onsubmit = function () {
                localStorage.setItem('feedback-sent', true);

                const feedbackData = {
                    gameScore: document.getElementById('score-game').valueAsNumber,
                    graphicScore: document.getElementById('score-graphic').valueAsNumber,
                    gameIdeaScore: document.getElementById('score-game-idea').valueAsNumber,
                    coopBattleScore: document.getElementById('score-co-op-battles').valueAsNumber,
                    fightingScore: document.getElementById('score-fighting').valueAsNumber,
                    craftingScore: document.getElementById('score-crafting').valueAsNumber,
                    tradingScore: document.getElementById('score-trading').valueAsNumber,
                    buildingScore: document.getElementById('score-building').valueAsNumber,
                    foundAnyBug: document.getElementById('feedback-any-bug-checkbox').checked,
                    bugDescription: document.getElementById('feedback-bug-description').value,
                    age: document.getElementById('feedback-age').valueAsNumber,
                    gender: document.getElementById('feedback-gender').value,
                    comment: document.getElementById('feedback-comment').value
                };

                if (JSON.stringify(feedbackData) == defaultFormData) {
                    Analytics.sendEvent("feedback.form.empty")
                } else {
                    Analytics.sendDataEvent("feedback.form", feedbackData)
                }

                userEventStream.publish('toggle-window', 'thank-you-window');
                return false;
            }.bind(this)
        }
    });
});