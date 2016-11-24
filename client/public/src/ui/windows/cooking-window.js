define((require) => {
    const Predicates = require('../../common/predicates');
    const userEventStream = require('../../component/dispatcher').userEventStream;
    const ItemStore = require('../../store/item');
    const CookingStore = require('../../store/cooking');

    function renderCost(cost) {
        const list = Object.keys(cost).map((itemKey) => {
            const itemName = ItemStore.byKey(itemKey).name;
            const itemCost = cost[itemKey];
            const numberOfItem = ItemStore.numberOfItem(itemKey);
            const liClass = (numberOfItem < itemCost) ? 'class="not-enough"' : '';
            return `<li ${liClass}>${itemName}: ${itemCost}</li>`
        }).join('');
        return `Cost: <ul class="cost">${list}</ul>`
    }

    function isEnoughItems(cost) {
        return Object.keys(cost).every((itemKey) => {
            const numberOfItem = ItemStore.numberOfItem(itemKey);
            return numberOfItem >= cost[itemKey];
        });
    }

    function renderRecipe(key, recipe) {
        return `<div class="element">
        <h3>${recipe.name}</h3>
        ${renderCost(recipe.cost)}
        ${isEnoughItems(recipe.cost) ?
            `<button data-recipe-key="${key}" class="cook-button">Cook & eat</button>` : ''
            }
</div>
`;
    }

    return {
        key: 'cooking-window',
        type: 'window',
        requirements: {
            playerAlive: Predicates.is(true),
        },
        classes: ['list-window'],
        attached(element) {
            const recipes = CookingStore.recipes;
            element.innerHTML = Object.keys(recipes).map(recipeKey => renderRecipe(recipeKey, recipes[recipeKey])).join('\n');
            const buildButtons = element.getElementsByClassName('cook-button');
            for (const buildButton of buildButtons) {
                buildButton.addEventListener('click', function () {
                    const recipeKey = this.getAttribute('data-recipe-key');
                    userEventStream.publish('toggle-window', 'cooking-window');
                    userEventStream.publish('action-started-cooking', recipes[recipeKey]);
                });
            }
        }
    };
});