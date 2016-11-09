define(function (require) {
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

    return createUiElement('cooking-window', {
        type: 'window',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
            }
        },
        created: function () {
            //noinspection JSUnusedGlobalSymbols
            this.className += 'list-window';
        },
        attached: function () {
            this._update();
        },
        detached: function () {
        },
        _update: function () {
            const recipes = CookingStore.recipes;
            this.innerHTML = Object.keys(recipes).map(recipeKey => renderRecipe(recipeKey, recipes[recipeKey])).join('\n');
            const buildButtons = this.getElementsByClassName('cook-button');
            for (const buildButton of buildButtons) {
                buildButton.addEventListener('click', function () {
                    const recipeKey = this.getAttribute('data-recipe-key');
                    userEventStream.publish('toggle-window', 'cooking-window');
                    userEventStream.publish('action-started-cooking', recipes[recipeKey]);
                });
            }
        }
    });
});