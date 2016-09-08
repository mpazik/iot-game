define(function (require) {
    const Achievements = require('../../component/achievement');

    var lastAchievement;

    function isUnlocked(achievementKey, achievementState) {
        return achievementState.achievementsUnlocked.has(achievementKey);
    }

    function renderAchievementItem(achievement, achievementState) {
        return `<li class="${isUnlocked(achievement.key, achievementState) ? 'unlocked' : ''}">
    <a href="${achievement.key}">${achievement.name}</a>
</li>`;
    }

    function renderAchievemnet(achievement, achievementState) {
        return `
<h1>${achievement.name}</h1>
${isUnlocked(achievement.key, achievementState) ? '<span class="unlocked">unlocked</span>' : ''}
<p>${achievement.description}</p>
`;
    }

    return createUiElement('achievement-window', {
        type: 'window',
        properties: {
            requirements: {
                achievementState: Predicates.is('connected')
            }
        },
        created: function () {
            this.innerHTML = `
<h1>Achievements</h1> 
<div class="list-with-preview">
    <ul></ul>
    <section></section>
</div>
`;
        },
        attached: function () {
            this._update(Achievements.achievementState.value);
            Achievements.achievementState.subscribe(this._update.bind(this));
        },
        detached: function () {
            Achievements.achievementState.unsubscribe(this._update.bind(this));
        },
        _update: function (achievementState) {
            const list = this.getElementsByTagName('ul')[0];
            const section = this.getElementsByTagName('section')[0];
            list.innerHTML = Achievements.achievementList.map(achievement => renderAchievementItem(achievement, achievementState)).join('\n');

            if (lastAchievement == null) {
                lastAchievement = Achievements.achievementList[0].key;
            }
            section.innerHTML = renderAchievemnet(Achievements.achievement(lastAchievement), achievementState);

            for (const a of this.getElementsByTagName('a')) {
                a.addEventListener('click', function (event) {
                    event.preventDefault();
                    lastAchievement = this.getAttribute('href');
                    section.innerHTML = renderAchievemnet(Achievements.achievement(lastAchievement), achievementState);
                })
            }
        }
    });
})
;