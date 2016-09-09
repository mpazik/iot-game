define(function (require) {
    const Achievement = require('../../component/achievement');

    function renderAchievementProgress(achievement) {
        const steps = achievement['unlock']['steps'];
        if (steps == null) return '';
        const progress = Achievement.state.achievementsProgress.get(achievement.key) || 0;
        return `<span>${progress}/${steps}</span>`;
    }

    function renderAchievement(achievement) {
        return `<li>${achievement['imperative']} ${renderAchievementProgress(achievement)}</li>`
    }

    return createUiElement('objective-tracker', {
        type: 'fragment',
        properties: {
            requirements: {
                achievementConnectionState: Predicates.is('connected')
            }
        },
        created: function () {
            this.innerHTML = `
<h3 style="margin-top: 0">Objectives</h3>
<ul></ul>
`;
        },
        attached: function () {
            this._updateStats();
            this.classList.add('area');
            Achievement.trackedAchievementPublisher.subscribe(this._updateStats.bind(this));
            Achievement.achievementChangePublisher.subscribe(Achievement.Changes.AchievementProgressed, this._updateStats.bind(this));
        },
        detached: function () {
            Achievement.trackedAchievementPublisher.unsubscribe(this._updateStats.bind(this));
            Achievement.achievementChangePublisher.unsubscribe(Achievement.Changes.AchievementProgressed, this._updateStats.bind(this));
        },
        _updateStats: function () {
            const trackedAchievements = Achievement.trackedAchievementPublisher.value;
            if (trackedAchievements.length == 0) {
                this.style.display = 'none';
                return
            } else if (this.style.display == 'none') {
                this.style.display = 'block';
            }
            const list = this.getElementsByTagName('ul')[0];
            list.innerHTML = trackedAchievements
                .map(key => Achievement.achievement(key))
                .map(renderAchievement)
                .join('')
        }
    });
});
