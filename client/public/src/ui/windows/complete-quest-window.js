define(function (require) {
    const Quest = require('../../store/quest');
    const Item = require('../../store/item');
    const Dispatcher = require('../../component/dispatcher');
    const objectKindById = require('../../store/resources').objectKind;

    function renderItemReward(reward) {
        const item = Item.byKey(reward['item']);
        return `${item.name}: ${reward['quantity']}`;
    }

    function renderItemRewards(itemRewards) {
        if (!itemRewards) {
            return '';
        }
        return `<div><h5>Items:</h5><ul>
      ${itemRewards.map(item => `<li>${renderItemReward(item)}</li>`).join('')}
</ul>`;
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
        if (!rewards || !rewards['items']) {
            return ''
        }
        const items = rewards['items'];
        return `<div class="rewards"><h4>Your rewards:</h4>
    ${renderItemRewards(rewards['items'])}
    ${renderBuildingRecipes(rewards['building_recipes'])}
</div>`
    }

    function lootItems(quest) {
        const rewards = quest['rewards'];
        if (!rewards || !rewards['loot_table']) {
            return
        }
        const lootTable = rewards['loot_table'];
        const lootedItems = Item.lootItems(lootTable);
        if (!rewards['items']) {
            rewards['items'] = lootedItems;
        } else {
            rewards['items'] = rewards['items'].concat(lootedItems);
        }

    }

    return createUiElement('complete-quest-window', {
        type: 'window',
        properties: {
            autoDisplay: true,
            requirements: {
                playerAlive: Predicates.is(true),
                completeQuestToDisplay: Predicates.isSet()
            }
        },
        created() {
        },
        attached() {
            const questKey = Quest.completeQuestToDisplay.value;
            const quest = Quest.questByKey(questKey);
            lootItems(quest);
            this.quest = quest;
            this.innerHTML = `
<h1>Quest completed</h1>
<div>Well done. You have done your quest: <b>${quest['title']}</b>.</div>
${renderRewards(quest['rewards'])}
<button class="large">Complete quest</button>
`;
            this.currentQuest = quest.key;
            const continueButton = this.getElementsByTagName('button')[0];
            continueButton.addEventListener('click', () => {
                Dispatcher.userEventStream.publish('toggle-window', 'complete-quest-window');
            })
        },
        detached (){
            deffer(() => Dispatcher.messageStream.publish('quest-completed', this.quest));
        }
    });
});