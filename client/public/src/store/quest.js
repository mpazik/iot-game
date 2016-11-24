define(function (require, exports, module) {
    const Resources = require('./resources');
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('../component/dispatcher');
    const Messages = require('../component/instance/messages');
    const Analytics = require('../component/analytics');
    const Item = require('./item');
    const Items = require('../common/model/items').Ids;

    var pushUpdateActiveQuests = null;
    var pushDisplayQuest = null;
    var pushUpdateQuestProgress = null;
    var pushDisplayCompletedQuest = null;
    const activeQuests = (() => {
        const data = localStorage.getItem('active-quests');
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
    var lastDisplayedQuest = (() => {
        const data = localStorage.getItem('last-displayed-quests');
        if (data) {
            try {
                return JSON.parse(data);
            } catch (e) {
                return 'welcome';
            }
        } else {
            return 'welcome'
        }
    })();
    const newQuestDelay = 1000;

    function questByKey(questKey) {
        return Resources.quests.find(quest => quest.key == questKey)
    }

    function initQuest(questKey) {
        const quest = questByKey(questKey);
        const requirement = quest['requirement'];
        const questStatus = {key: questKey};
        switch (requirement['type']) {
            case 'sub-quests': {
                (function () {
                    const quests = requirement['quests'];
                    questStatus.progress = 0;
                    questStatus.goal = quests.length;
                    setTimeout(() => displayQuest(quests[0]), newQuestDelay);
                })();
                break;
            }
            case 'message':
            case 'user-event': {
                (function () {
                    if (requirement['repetition']) {
                        questStatus.progress = 0;
                        questStatus.goal = requirement['repetition']
                    }
                })();
                break;
            }
            case 'items-count':
                (function () {
                    questStatus.goal = requirement['quantity'];
                })();
                break;
        }
        Analytics.sendDataEvent("quest.started", {key: questKey});
        activeQuests.push(questStatus);
        localStorage.setItem('active-quests', JSON.stringify(activeQuests));
        pushUpdateActiveQuests(activeQuests.slice());
        trackQuest(questStatus);
    }

    function trackQuest(questStatus) {
        const quest = questByKey(questStatus.key);
        const requirement = quest['requirement'];
        switch (requirement['type']) {
            case 'sub-quests': {
                (function () {
                    const quests = requirement['quests'];
                    Dispatcher.messageStream.subscribe('quest-completed', subscription);

                    function subscription(quest) {
                        if (quests.includes(quest.key)) {
                            const newProgress = questStatus.progress + 1;
                            if (quests[newProgress]) {
                                setTimeout(() => displayQuest(quests[newProgress]), newQuestDelay);
                            }
                            updateQuestStatusProgress(newProgress, unsubscribe);
                        }
                    }

                    function unsubscribe() {
                        Dispatcher.messageStream.unsubscribe('quest-completed', subscription)
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

                    Dispatcher[stream].subscribe(eventType, subscription);

                    function subscription(event) {
                        if (requirement['values'] && !containsValues(event, requirement['values'])) {
                            return
                        }
                        if (requirement['repetition']) {
                            updateQuestStatusProgress(questStatus.progress + 1, unsubscribe);
                        } else {
                            unsubscribe();
                            completeQuest(questStatus.key);
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
        localStorage.setItem('active-quests', JSON.stringify(activeQuests));
        pushUpdateActiveQuests(activeQuests.slice());
        Analytics.sendDataEvent("quest.complete", {key: questKey});

        pushDisplayCompletedQuest(questKey);
    }

    Dispatcher.userEventStream.subscribe('quest-started', (questKey) => {
        if (activeQuests.findIndex(questStatus => questStatus.key == questKey) < 0) {
            initQuest(questKey);
        }
    });

    Dispatcher.messageStream.subscribe('quest-completed', quest => {
        pushDisplayCompletedQuest(null);
        if (!quest['rewards'] || !quest['rewards']['quest']) {
            return;
        }
        const nextQuest = quest['rewards']['quest'];
        setTimeout(() => displayQuest(nextQuest), newQuestDelay);
    });

    function displayQuest(questKey) {
        lastDisplayedQuest = questKey;
        localStorage.setItem('last-displayed-quests', JSON.stringify(lastDisplayedQuest));
        pushDisplayQuest(questKey);
    }

    module.exports = {
        init() {
            activeQuests.forEach(trackQuest);
        },
        activeQuests: new Publisher.StatePublisher(activeQuests, (push) => {
            pushUpdateActiveQuests = push
        }),
        questToDisplay: new Publisher.StatePublisher(lastDisplayedQuest, (push) => {
            pushDisplayQuest = push
        }),
        displayQuest,
        completeQuestToDisplay: new Publisher.StatePublisher(null, (push) => {
            pushDisplayCompletedQuest = push
        }),
        questProgress: new Publisher.StatePublisher(lastDisplayedQuest, (push) => {
            pushUpdateQuestProgress = push
        }),
        questByKey
    };
});