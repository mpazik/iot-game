define(function (require) {
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

    return createUiElement('objective-tracker', {
        type: 'fragment',
        properties: {
            requirements: {
                activeQuests: isNonEmpty
            }
        },
        created: function () {
            this.classList.add('area');
            this.innerHTML = `
<h3 style="margin-top: 0">Objectives</h3>
<ul></ul>
`;
        },
        attached: function () {
            this._updateQuests(Quest.activeQuests.value);
            Quest.activeQuests.subscribe(this._updateQuests.bind(this));
            Quest.questProgress.subscribe(this._updateQuestProgress.bind(this));
        },
        detached: function () {
            Quest.activeQuests.unsubscribe(this._updateQuests.bind(this));
            Quest.questProgress.unsubscribe(this._updateQuestProgress.bind(this));
        },
        _updateQuestProgress(questStatus) {
            const questElement = document.getElementById(questId(questStatus.key));
            questElement.innerHTML = renderQuestContent(Quest.questByKey(questStatus.key), questStatus);
            const anchor = questElement.getElementsByTagName('a')[0];
            anchor.addEventListener('click', () => {
                const questKey = anchor.getAttribute('data-quest-key');
                Quest.displayQuest(questKey);
            });
        },
        _updateQuests(activeQuests) {
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
    });
});
