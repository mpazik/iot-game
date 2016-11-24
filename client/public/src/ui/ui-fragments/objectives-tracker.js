define((require) => {
    const Quest = require('../../store/quest');

    function renderQuestProgress(status) {
        const goal = status['goal'];
        if (!goal) {
            return '';
        }
        return `<span>${status['progress']}/${goal}</span>`;
    }

    function questId(questKey) {
        return 'quest-status_' + questKey;
    }

    function renderQuestContent(quest, status) {
        return `<a data-quest-key="${quest['key']}">${quest['imperative']}</a> ${renderQuestProgress(status)}`;
    }

    function renderQuest(quest, status) {
        return `<li id="${questId(quest.key)}">${renderQuestContent(quest, status)}</li>`
    }

    function isNonEmpty(array) {
        return array.length > 0;
    }

    function updateQuestProgress(questStatus) {
        const questElement = document.getElementById(questId(questStatus.key));
        questElement.innerHTML = renderQuestContent(Quest.questByKey(questStatus.key), questStatus);
        const anchor = questElement.getElementsByTagName('a')[0];
        anchor.addEventListener('click', () => {
            const questKey = anchor.getAttribute('data-quest-key');
            Quest.displayQuest(questKey);
        });
    }

    function updateQuests(activeQuests) {
        const list = this.getElementsByTagName('ul')[0];
        list.innerHTML = activeQuests.map(questStatus => {
            return renderQuest(Quest.questByKey(questStatus.key), questStatus)
        }).join('');
        for (let anchor of this.getElementsByTagName('a')) {
            anchor.addEventListener('click', () => {
                const questKey = anchor.getAttribute('data-quest-key');
                Quest.displayQuest(questKey);
            })
        }
    }

    return {
        key: 'objective-tracker',
        type: 'fragment',
        requirements: {
            activeQuests: isNonEmpty,
            playerAlive: Predicates.is(true)
        },
        template: `
<h3 style="margin-top: 0">Objectives</h3>
<ul></ul>
`,
        classes: ['area'],
        attached(element) {
            element.updateQuests = updateQuests.bind(element);
            element.updateQuestProgress = updateQuestProgress.bind(element);
            Quest.activeQuests.subscribeAndTrigger(element.updateQuests);
            Quest.questProgress.subscribe(element.updateQuestProgress);
        },
        detached(element) {
            Quest.activeQuests.unsubscribe(element.updateQuests);
            Quest.questProgress.unsubscribe(element.updateQuestProgress);
        }
    };
});
