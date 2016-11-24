define((require) => {
    const Predicates = require('../../common/predicates');
    const Quest = require('../../store/quest');
    const Item = require('../../store/item');
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const objectKindById = require('../../store/resources').objectKind;

    function renderItemReward(reward) {
        const item = Item.byKey(reward['item']);
        return `${item.name}: ${reward['quantity']}`;
    }

    function renderItemRewards(itemRewards) {
        if (!itemRewards) {
            return '';
        }
        return `<ul>
      ${itemRewards.map(item => `<li>${renderItemReward(item)}</li>`).join('')}
</ul>`;
    }

    function renderLootTable(lootTable) {
        if (!lootTable) {
            return '';
        }
        return `<div>Some mysterious items</div>`;
    }

    function renderItems(rewards) {
        const items = renderItemRewards(rewards['items']);
        const lootTable = renderLootTable(rewards['loot_table']);
        if (items == '' && lootTable == '') {
            return ''
        }
        return `<div><h5>Items:</h5>
    ${items}
    ${lootTable}
</div>        
`
    }

    function renderBuildingRecipes(recipes) {
        if (!recipes) {
            return '';
        }
        return `<div><h5>Building recipes:</h5><ul>
    ${recipes.map(buildingKindId => `<li>${objectKindById(buildingKindId)['name']}</li>`).join('')}
</ul>
</div>`;
    }

    function renderRewards(rewards) {
        if (!rewards || (Object.keys(rewards).length == 1 && Object.keys(rewards)[0] == 'quest')) {
            return ''
        }

        return `<div class="rewards"><h4>Rewards:</h4>
    ${renderItems(rewards)}
    ${renderBuildingRecipes(rewards['building_recipes'])}
</div>`
    }

    return {
        key: 'start-quest-window',
        type: 'window',
        autoDisplay: true,
        requirements: {
            playerAlive: Predicates.is(true),
            questToDisplay: Predicates.isSet()
        },
        attached(element) {
            const questKey = Quest.questToDisplay.value;
            const quest = Quest.questByKey(questKey);
            element.innerHTML = `
<h1>${quest['title']}</h1>
<div>${quest['content']}</div>
${renderRewards(quest['rewards'])}
<button class="large">Continue</button>
`;
            element.currentQuest = quest.key;
            const continueButton = element.getElementsByTagName('button')[0];
            continueButton.addEventListener('click', () => {
                userEventStream.publish('toggle-window', 'start-quest-window');
            })
        },
        detached(element) {
            Quest.displayQuest(null);
            if (!Quest.activeQuests.value.some(quest => quest.key == element.currentQuest)) {
                deffer(() => userEventStream.publish('quest-started', element.currentQuest));
            }
        }
    };
});