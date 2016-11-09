define(function (require) {
    const objectKindById = require('../../store/resources').objectKind;
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const Skills = require('../../common/model/skills');
    const ActionBar = require('../../store/action-bar');

    function renderObject(objectKind) {
        // if there are growing steps, display the last one
        const sprite = objectKind['growingSteps'] ? objectKind['sprite'] + '-icon' : objectKind['sprite'];
        return `<div class="object">
    <div class="object-icon ${sprite}"></div>
    <div>
        <h3>${objectKind['name']}</h3>
        <button data-object-kind-id="${objectKind['id']}" class="build-button">Build</button>
    </div>
    <div style="clear: both"></div>
</div>
`;
    }

    return createUiElement('building-window', {
        type: 'window',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
            }
        },
        created: function () {
            this.innerHTML = `<div></div>`;
        },
        attached: function () {
            this._update();
        },
        detached: function () {
        },
        _update: function () {
            this.innerHTML = ActionBar.unlockedObjects.map(objectKindId => renderObject(objectKindById(objectKindId))).join('\n');
            const buildButtons = this.getElementsByClassName('build-button');
            for (const buildButton of buildButtons) {
                buildButton.addEventListener('click', function () {
                    const objectKindId = this.getAttribute('data-object-kind-id');
                    userEventStream.publish('toggle-window', 'building-window');
                    userEventStream.publish('skill-triggered', {
                        skill: {
                            id: 1,
                            type: Skills.Types.BUILD,
                            objectKind: objectKindId
                        }
                    });
                });
            }
        }
    });
});