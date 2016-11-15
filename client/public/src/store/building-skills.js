define(function (require, exports, module) {
    const objectKindById = require('./resources').objectKind;
    const Dispatcher = require('../component/dispatcher');
    const CharacterNotification = require('./character-notification');

    const recipes = [8];

    Dispatcher.messageStream.subscribe('quest-completed', quest => {
        const rewards = quest['rewards'];
        if (!rewards || !rewards['building_recipes']) {
            return
        }
        rewards['building_recipes'].forEach((buildingId) => {
            recipes.push(buildingId);
            const objectKind = objectKindById(buildingId);
            CharacterNotification.notify(`learnt building: ${objectKind['name']}`)
        });
    });

    module.exports = {
        recipes
    }
});
