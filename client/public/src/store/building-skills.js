define(function (require, exports, module) {
    const objectKindById = require('./resources').objectKind;
    const Dispatcher = require('../component/dispatcher');
    const CharacterNotification = require('./character-notification');

    const recipes = (() => {
        const data = localStorage.getItem('building-recipes');
        if (data) {
            try {
                return JSON.parse(data);
            } catch (e) {
                return [];
            }
        } else {
            return []
        }
    })();

    Dispatcher.messageStream.subscribe('quest-completed', quest => {
        const rewards = quest['rewards'];
        if (!rewards || !rewards['building_recipes']) {
            return
        }
        rewards['building_recipes'].forEach((buildingId) => {
            if (recipes.includes(buildingId)) {
                return;
            }
            recipes.push(buildingId);
            localStorage.setItem('building-recipes', JSON.stringify(recipes));
            const objectKind = objectKindById(buildingId);
            CharacterNotification.notify(`learnt building: ${objectKind['name']}`);
        });
    });

    module.exports = {
        recipes
    }
});
