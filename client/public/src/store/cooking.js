define(function (require, exports, module) {
    const Dispatcher = require('../component/dispatcher');
    const Render = require('../render/render');
    const CharacterNotification = require('./character-notification');

    const recipes = {
        snack: {
            name: 'Snack',
            cost: {
                'CORN': 2,
            },
            effect: 'invert',
            cookingTime: 1000
        },
        tomatoMeal: {
            name: 'Tomato meal',
            cost: {
                'CORN': 5,
                'TOMATO': 3,
            },
            effect: 'twist',
            cookingTime: 5000
        },
        paprikaMeal: {
            name: 'Paprika meal',
            cost: {
                'CORN': 5,
                'PAPRIKA': 3,
            },
            effect: 'twist',
            cookingTime: 5000
        }
    };


    Dispatcher.messageStream.subscribe('action-cooked', (recipe) => {
        const forkAndKnifeEmoji = '🍴';
        CharacterNotification.notify(forkAndKnifeEmoji + recipe.name);
        Render.runEffect(recipe.effect);
    });

    module.exports = {
        recipes
    }
});

