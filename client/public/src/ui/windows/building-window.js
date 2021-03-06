define((require) => {
    const Predicates = require('../../common/predicates');
    const KeyCodes = require('../../common/key-codes');
    const objectKindById = require('../../store/resources').objectKind;
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const Skills = require('../../common/model/skills');
    const BuildingSkills = require('../../store/building-skills');
    const ItemStore = require('../../store/item');

    function renderCost(objectKind) {
        const cost = objectKind['cost'];
        if (!cost) {
            return ''
        }
        const list = Object.keys(cost).map((itemKey) => {
            const itemName = ItemStore.byKey(itemKey).name;
            const itemCost = cost[itemKey];
            const numberOfItem = ItemStore.numberOfItem(itemKey);
            const liClass = (numberOfItem < itemCost) ? 'class="not-enough"' : '';
            return `<li ${liClass}>${itemName}: ${itemCost}</li>`
        }).join('');
        return `<ul class="cost">${list}</ul>`
    }

    function isEnoughItems(objectKind) {
        const cost = objectKind['cost'];
        if (!cost) {
            return true;
        }
        return Object.keys(cost).every((itemKey) => {
            const numberOfItem = ItemStore.numberOfItem(itemKey);
            return numberOfItem >= cost[itemKey];
        });
    }

    function getSprite(objectKind) {
        if (objectKind['width'] < 5 && objectKind['height'] < 5) {
            return objectKind['sprite'] + '-icon';
        }
        if (objectKind['growingSteps']) {
            return objectKind['sprite'] + objectKind['growingSteps'];
        }
        if (objectKind['animationSteps']) {
            return objectKind['sprite'] + '1';
        }
        return objectKind['sprite'];
    }

    function renderObject(objectKind) {
        return `<div class="element">
    <div class="object-icon ${getSprite(objectKind)}"></div>
    <div class="object-description">
        <h3>${objectKind['name']}</h3>
        ${renderCost(objectKind)}
        ${isEnoughItems(objectKind) ?
            `<button data-object-kind-id="${objectKind['id']}" class="build-button">Build</button>` : ''
            }
    </div>
    <div style="clear: both"></div>
</div>
`;
    }

    return {
        key: 'building-window',
        type: 'window',
        activateKeyBind: KeyCodes.fromLetter('B'),
        requirements: {
            isPlayerOnOwnParcel: Predicates.is(true),
            playerAlive: Predicates.is(true),
        },
        classes: ['list-window'],
        attached(element) {
            if (BuildingSkills.recipes.length == 0) {
                this.innerHTML = 'You don\'t know any building recipe';
                return;
            }
            element.innerHTML = BuildingSkills.recipes.map(objectKindId => renderObject(objectKindById(objectKindId))).join('\n');
            const buildButtons = element.getElementsByClassName('build-button');
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
    }
})
;