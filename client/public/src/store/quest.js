define(function (require, exports, module) {
    const Resources = require('./resources');
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../component/instance/messages');
    const Item = require('./item');
    const Items = require('../common/model/items').Ids;

    var pushUpdateActiveQuests = null;
    var pushDisplayQuest = null;
    var pushUpdateQuestProgress = null;
    var pushDisplayCompletedQuest = null;
    const activeQuests = [];
    const initialQuest = null;
    const newQuestDelay = 1000;

    function questByKey(questKey) {
        return Resources.quests.find(quest => quest.key == questKey)
    }

    function trackQuest(questKey) {
        const quest = questByKey(questKey);
        const requirement = quest['requirement'];
        const questStatus = {key: questKey};
        switch (requirement['type']) {
            case 'sub-quests': {
                (function () {
                    const quests = requirement['quests'];
                    questStatus.progress = 0;
                    questStatus.goal = quests.length;
                    setTimeout(() => pushDisplayQuest(quests[0]), newQuestDelay);
                    Dispatcher.messageStream.subscribe('quest-completed', subscription);

                    function subscription(quest) {
                        if (quests.includes(quest.key)) {
                            const newProgress = questStatus.progress + 1;
                            if (quests[newProgress]) {
                                setTimeout(() => pushDisplayQuest(quests[newProgress]), newQuestDelay);
                            }
                            updateQuestStatusProgress(newProgress, completeQuest);
                        }
                    }

                    function unsubscribe() {
                        Dispatcher.messageStream.unsubscribe(message, subscription)
                    }
                })();
                break;
            }
            case 'message':
            case 'user-event': {
                (function () {
                    const stream = requirement['type'] == 'user-event' ? 'userEventStream' : 'messageStream';
                    const eventType = requirement['messageClass'] ? Messages[requirement['messageClass']] : requirement['key'];
                    if (!eventType) {
                        console.error('Quest had undefined event type', requirement);
                        return
                    }
                    if (requirement['repetition']) {
                        questStatus.progress = 0;
                        questStatus.goal = requirement['repetition']
                    }

                    Dispatcher[stream].subscribe(eventType, subscription);

                    function subscription(event) {
                        if (requirement['values'] && !containsValues(event, requirement['values'])) {
                            return
                        }
                        if (requirement['repetition']) {
                            updateQuestStatusProgress(questStatus.progress + 1, unsubscribe);
                        } else {
                            unsubscribe();
                            completeQuest(questKey);
                        }
                    }

                    function containsValues(object, valuesToCheck) {
                        for (const key of Object.keys(valuesToCheck)) {
                            if (object[key] != valuesToCheck[key]) {
                                return false;
                            }
                        }
                        return true;
                    }

                    function unsubscribe() {
                        Dispatcher[stream].unsubscribe(eventType, subscription)
                    }
                })();
                break;
            }
            case 'items-count':
                (function () {
                    questStatus.goal = requirement['quantity'];
                    Item.itemsChange.subscribe(subscription);
                    deffer(() => updateQuestStatusProgress(Item.numberOfItem(requirement['item']), unsubscribe));

                    function subscription(itemChange) {
                        if (itemChange.item == Items[requirement['item']]) {
                            updateQuestStatusProgress(itemChange.quantity, unsubscribe);
                        }
                    }

                    function unsubscribe() {
                        Item.itemsChange.unsubscribe(subscription);
                    }
                })();
                break;
        }
        activeQuests.push(questStatus);
        pushUpdateActiveQuests(activeQuests.slice());

        function updateQuestStatusProgress(progress, onComplete) {
            questStatus.progress = progress;
            if (questStatus.progress >= questStatus.goal) {
                onComplete();
                completeQuest(questStatus.key);
            } else {
                // cloning an object because if reference is same publish is ignored
                pushUpdateQuestProgress(Object.assign({}, questStatus));
            }
        }
    }

    function completeQuest(questKey) {
        // remove from active quest
        const index = activeQuests.findIndex(questStatus => questStatus.key == questKey);
        activeQuests.splice(index, 1);
        pushUpdateActiveQuests(activeQuests.slice());

        pushDisplayCompletedQuest(questKey);
    }

    Dispatcher.userEventStream.subscribe('quest-started', (questKey) => {
        if (!activeQuests.includes(questKey)) {
            trackQuest(questKey);
        }
    });

    Dispatcher.messageStream.subscribe('quest-completed', quest => {
        pushDisplayCompletedQuest(null);
        if (!quest['rewards'] || !quest['rewards']['quest']) {
            return;
        }
        const nextQuest = quest['rewards']['quest'];
        setTimeout(() => pushDisplayQuest(nextQuest), newQuestDelay);
    });

    module.exports = {
        activeQuests: new Publisher.StatePublisher(activeQuests, (push) => {
            pushUpdateActiveQuests = push
        }),
        questToDisplay: new Publisher.StatePublisher(initialQuest, (push) => {
            pushDisplayQuest = push
        }),
        displayQuest: pushDisplayQuest,
        completeQuestToDisplay: new Publisher.StatePublisher(null, (push) => {
            pushDisplayCompletedQuest = push
        }),
        questProgress: new Publisher.StatePublisher(initialQuest, (push) => {
            pushUpdateQuestProgress = push
        }),
        questByKey
    };
});