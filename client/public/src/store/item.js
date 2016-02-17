define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MessageIds = require('../common/packet/messages').ids;
    const Messages = require('../common/packet/messages').constructors;
    const ResourcesStore = require('./resources');
    const MainPlayerStore = require('./main-player');
    const Dispatcher = require('../component/dispatcher');
    const Skills = require('../common/model/skills');

    var items = {
        1: 4
    };
    var pushEvent;

    Dispatcher.userEventStream.subscribe('skill-triggered', function (event) {
        const skill = event.skill;
        if (skill.type != Skills.Types.CRAFT) return;

        if (checkSkillItemRequirements(skill.id)) {
            const skillUsed = new Messages.SkillUsed(MainPlayerStore.characterId(), skill.id);
            Dispatcher.messageStream.publish(MessageIds.SkillUsed, skillUsed);
        }
    });

    Dispatcher.messageStream.subscribe(MessageIds.SkillUsed, function (event) {
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

    Dispatcher.messageStream.subscribe(MessageIds.CharacterDied, (event) => {
        if (event.casterId != MainPlayerStore.characterId()) return;

        Object.keys(quantities).forEach(item => {
            changeItemQuantity(item, -items[item])
        });
    });

    Dispatcher.messageStream.subscribe(MessageIds.SkillUsedOnWorldObject, (event) => {
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

        // clone array in order to omit equality check in the publisher
        const newObject = {};
        Object.extend(newObject, items);
        pushEvent(newObject);
    }

    const getItemQuantity = (item) => items[item] ? items[item] : 0;

    function checkSkillItemRequirements(skillId) {
        var serverMessage;
        if (MainPlayerStore.playerCooldown.value != null) {
            serverMessage = new Messages.ServerMessage("You are not ready yet to use ability", Messages.ServerMessage.Kinds.INFO);
            Dispatcher.messageStream.publish(MessageIds.ServerMessage, serverMessage);
            return false;
        }
        const skill = ResourcesStore.skill(skillId);
        if (skill.itemsRequired) {
            for (var itemRequirements of skill.itemsRequired) {
                var item = ResourcesStore.item(itemRequirements.item);
                if (getItemQuantity(itemRequirements.item) < itemRequirements.quantity) {
                    serverMessage = new Messages.ServerMessage(`Not enough number of ${item.name}s. Required ${itemRequirements.quantity}`, Messages.ServerMessage.Kinds.INFO);
                    Dispatcher.messageStream.publish(MessageIds.ServerMessage, serverMessage);
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