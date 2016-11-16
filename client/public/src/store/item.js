define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Messages = require('../component/instance/messages');
    const ResourcesStore = require('./resources');
    const MainPlayerStore = require('./main-player');
    const Dispatcher = require('../component/dispatcher');
    const Items = require('../common/model/items').Ids;
    const CharacterNotification = require('./character-notification');

    var playerItems = (() => {
        const data = localStorage.getItem('items');
        if (data) {
            try {
                const items = JSON.parse(data);

                function deleteItem(item) {
                    if (items[item]) {
                        delete items[item];
                    }
                }

                [Items.APPLE, Items.ARROW, Items.STICK].forEach(deleteItem);

                return items;
            } catch (e) {
                return {};
            }
        } else {
            return {}
        }
    })();
    var pushItemChange;

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

    function payItemCost(cost) {
        Object.keys(cost).forEach((itemKey) => {
            const itemId = Items[itemKey];
            const itemCost = cost[itemKey];
            changeItemQuantity(itemId, -itemCost);
        });
    }

    Dispatcher.userEventStream.subscribe('build-object', (event) => {
        const objectKind = ResourcesStore.objectKind(event.objectKindId);
        const cost = objectKind['cost'];
        if (!cost) {
            return
        }
        payItemCost(cost);
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
                    changeItemQuantity(Items.CORN, 2);
                    break;
                case 'paprika':
                    changeItemQuantity(Items.PAPRIKA, 1);
            }
        }
    });

    Dispatcher.messageStream.subscribe('action-cooked', (recipe) => {
        payItemCost(recipe.cost);
    });

    Dispatcher.messageStream.subscribe('quest-completed', quest => {
        const rewards = quest['rewards'];
        if (rewards && rewards['items']) {
            changeItemsQuantity(rewards['items'])
        }
    });

    function changeItemsQuantity(itemsChanges) {
        itemsChanges.forEach(itemChange => {
            changeItemQuantity(Items[itemChange.item], itemChange.quantity)
        })
    }

    function partitionArray(array, predicate) {
        const falseArray = [];
        const trueArray = [];
        array.forEach(element => {
            if (predicate(element)) {
                falseArray.push(element);
            } else {
                trueArray.push(element);
            }
        });
        return [falseArray, trueArray]
    }

    function changeItemQuantity(item, quantityChange) {
        if (playerItems[item]) {
            playerItems[item] += quantityChange
        } else {
            playerItems[item] = quantityChange;
        }
        localStorage.setItem('items', JSON.stringify(playerItems));

        pushItemChange({item, quantity: playerItems[item], quantityChange});
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
        lootItems(lootTable) {
            const result = partitionArray(lootTable, (row) => !row['weight']);
            const itemsLooted = result[0].reduce((items, row) => items.concat(row['items']), []);
            const rowsToLoot = result[1];

            return itemsLooted.concat(chooseByWeight());

            function chooseByWeight() {
                const weightSum = rowsToLoot.reduce((sum, row) =>sum + row['weight'], 0);
                const chosenWeight = Math.round(Math.random() * weightSum);
                let currentSum = 0;
                for (let row of rowsToLoot) {
                    currentSum += row['weight'];
                    if (currentSum > chosenWeight) {
                        return row['items'];
                    }
                }
                throw 'error in algorithm'
            }
        },
        playerItems,
        itemsChange: new Publisher.StatePublisher(playerItems, (push) => pushItemChange = push)
    };
});