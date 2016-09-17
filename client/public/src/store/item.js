define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Messages = require('../component/instance/messages');
    const ResourcesStore = require('./resources');
    const MainPlayerStore = require('./main-player');
    const Dispatcher = require('../component/dispatcher');

    var items = (() => {
        const items = localStorage.getItem('items');
        if (items) {
            try {
                return JSON.parse(items);
            } catch (e) {
                return {};
            }
        } else {
            return {}
        }
    })();
    var pushEvent;

    Dispatcher.messageStream.subscribe(Messages.SkillUsed, function (event) {
        if (event.casterId != MainPlayerStore.characterId()) return;

        const skill = ResourcesStore.skill(event.skillId);
        if (skill.itemsProduced) {
            skill.itemsProduced.forEach(itemToAdd => {
                changeItemQuantity(itemToAdd.item, itemToAdd.quantity)
            });
        }
        if (skill.itemsRequired) {
            skill.itemsRequired.forEach(itemToRemove => {
                changeItemQuantity(itemToRemove.item, -itemToRemove.quantity)
            });
        }
    });

    Dispatcher.messageStream.subscribe(Messages.CharacterDied, (event) => {
        if (event.casterId != MainPlayerStore.characterId()) return;

        Object.keys(quantities).forEach(item => {
            changeItemQuantity(item, -items[item])
        });
    });

    Dispatcher.messageStream.subscribe(Messages.SkillUsedOnWorldObject, (event) => {
        if (event.casterId != MainPlayerStore.characterId()) return;

        const skill = ResourcesStore.skill(event.skillId);
        if (skill.itemsProduced) {
            skill.itemsProduced.forEach(itemToAdd => {
                changeItemQuantity(itemToAdd.item, itemToAdd.quantity)
            });
        }
        if (skill.itemsRequired) {
            skill.itemsRequired.forEach(itemToRemove => {
                changeItemQuantity(itemToRemove.item, -itemToRemove.quantity)
            });
        }
    });

    function changeItemQuantity(item, quantityChange) {
        if (items[item]) {
            items[item] += quantityChange
        } else {
            items[item] = quantityChange;
        }
        localStorage.setItem('items', JSON.stringify(items));

        // clone array in order to omit equality check in the publisher
        const newObject = {};
        Object.assign(newObject, items);
        pushEvent(newObject);
    }

    const getItemQuantity = (item) => items[item] ? items[item] : 0;

    function checkSkillItemRequirements(skillId) {
        var serverMessage;
        if (MainPlayerStore.playerCooldown.value != null) {
            serverMessage = new Messages.ServerMessage("You are not ready yet to use ability");
            Dispatcher.messageStream.publish(Messages.ServerMessage, serverMessage);
            return false;
        }
        const skill = ResourcesStore.skill(skillId);
        if (skill.itemsRequired) {
            for (var itemRequirements of skill.itemsRequired) {
                var item = ResourcesStore.item(itemRequirements.item);
                if (getItemQuantity(itemRequirements.item) < itemRequirements.quantity) {
                    serverMessage = new Messages.ServerMessage(`Not enough number of ${item.name}s. Required ${itemRequirements.quantity}`);
                    Dispatcher.messageStream.publish(Messages.ServerMessage, serverMessage);
                    return false;
                }
            }
        }
        return true;
    }

    module.exports = {
        checkSkillItemRequirements,
        itemsChange: new Publisher.StatePublisher(items, (push) => pushEvent = push)
    };
});