define(function (require) {
    const Achievements = require('../../component/achievement');

    function isUnlocked(achievementKey) {
        return Achievements.state.achievementsUnlocked.has(achievementKey);
    }

    function renderAchievement(achievement) {
        return `<div class="achievement ${isUnlocked(achievement.key) ? 'unlocked' : ''}">
<h3>${achievement['title']}</h3>
<span>${achievement['imperative']}</span>
</div>
`;
    }

    return createUiElement('achievement-window', {
        type: 'window',
        properties: {
            requirements: {
                achievementConnectionState: Predicates.is('connected')
            }
        },
        created: function () {
            this.innerHTML = `
<h1>Achievements</h1> 
<div>
    
</div>
`;
        },
        attached: function () {
            this._update(Achievements.achievementChangePublisher.value);
            Achievements.achievementChangePublisher.subscribe(this._update.bind(this));
        },
        detached: function () {
            Achievements.achievementChangePublisher.unsubscribe(this._update.bind(this));
        },
        _update: function () {
            const list = this.getElementsByTagName('div')[0];
            list.innerHTML = Achievements.achievementList.map(achievement => renderAchievement(achievement)).join('\n');
        }
    });
});