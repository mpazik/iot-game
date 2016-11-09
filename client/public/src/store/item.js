define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Messages = require('../component/instance/messages');
    const ResourcesStore = require('./resources');
    const MainPlayerStore = require('./main-player');
    const Dispatcher = require('../component/dispatcher');
    const Items = require('../common/model/items').Ids;
    const CharacterNotification = require('./character-notification');

    var playerItems = (() => {
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
            changeItemQuantity(item, -playerItems[item])
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

    Dispatcher.userEventStream.subscribe('build-object', (event) => {
        const objectKind = ResourcesStore.objectKind(event.objectKindId);
        const cost = objectKind['cost'];
        if (!cost) {
            return
        }
        Object.keys(cost).forEach((itemKey) => {
            const itemId = Items[itemKey];
            const itemCost = cost[itemKey];
            changeItemQuantity(itemId, -itemCost);
        });
    });

    Dispatcher.messageStream.subscribe('action-completed-on-world-object', (event) => {
        if (event.action.key == 'cut-tree') {
            changeItemQuantity(Items.WOOD, 10);
        } else if (event.action.key == 'harvest') {
            switch (event.worldObjectKind.key) {
                case 'tomato':
                    changeItemQuantity(Items.TOMATO, 1);
                    break;
                case 'corn':
                    changeItemQuantity(Items.CORN, 1);
                    break;
                case 'paprika':
                    changeItemQuantity(Items.PAPRIKA, 1);
            }
        }
    });

    function changeItemQuantity(item, quantityChange) {
        if (playerItems[item]) {
            playerItems[item] += quantityChange
        } else {
            playerItems[item] = quantityChange;
        }
        localStorage.setItem('items', JSON.stringify(playerItems));

        // clone array in order to omit equality check in the publisher
        const newObject = {};
        Object.assign(newObject, playerItems);
        pushEvent(newObject);
        if (quantityChange > 0) {
            CharacterNotification.notify(ResourcesStore.item(item).name + ' +' + quantityChange);
        }
    }

    const getItemQuantity = (item) => playerItems[item] ? playerItems[item] : 0;

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
        byKey(itemKey) {
            const itemId = Items[itemKey];
            return ResourcesStore.item(itemId);
        },
        numberOfItem(itemKey) {
            const itemId = Items[itemKey];
            return playerItems[itemId] ? playerItems[itemId] : 0;
        },
        itemsChange: new Publisher.StatePublisher(playerItems, (push) => pushEvent = push)
    };
});