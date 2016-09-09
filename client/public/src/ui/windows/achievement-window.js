define(function (require) {
    const Achievement = require('../../component/achievement');
    const skillById = require('../../store/resources').skill;
    const Skills = require('../../common/model/skills');

    function isUnlocked(achievementKey) {
        return Achievement.state.achievementsUnlocked.has(achievementKey);
    }

    function renderUnlockDate(achievement) {
        const unlockEpochDate = Achievement.state.achievementsUnlocked.get(achievement.key);
        if (unlockEpochDate == null) return '';
        return `<span class="achievement-unlock-date">${new Date(unlockEpochDate).toLocaleDateString()}</span>`;
    }

    function renderAchievementProgress(achievement) {
        const steps = achievement['unlock']['steps'];
        if (steps == null) return '';

        const progress = Achievement.state.achievementsUnlocked.has(achievement.key) ? steps : Achievement.state.achievementsProgress.get(achievement.key) || 0;
        return `<span class="achievement-progress">${progress}/${steps}</span>`;
    }

    function skillByKey(skillKey) {
        return skillById(Skills.keyToId(skillKey))
    }

    function renderRewards(achievement) {
        const rewards = achievement['rewards'];
        if (rewards == null) return '';
        const rewardsHtml = rewards.map(reward => `<span>${skillByKey(reward['skillKey']).name}(${reward['type']})</span>`).join(', ');
        return `<div class="achievement-rewards">Reward: ${rewardsHtml}</div>`;
    }

    function renderAchievement(achievement) {
        return `<div class="achievement ${isUnlocked(achievement.key) ? 'unlocked' : ''}">
<h3>${achievement['title']}</h3>
${renderUnlockDate(achievement)}
<span>${achievement['imperative']}</span>
${renderAchievementProgress(achievement)}
${renderRewards(achievement)}
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
            this._update(Achievement.achievementChangePublisher.value);
            Achievement.achievementChangePublisher.subscribe(Achievement.Changes.AchievementUnlocked, this._update.bind(this));
            Achievement.achievementChangePublisher.subscribe(Achievement.Changes.AchievementProgressed, this._update.bind(this));
        },
        detached: function () {
            Achievement.achievementChangePublisher.unsubscribe(Achievement.Changes.AchievementUnlocked, this._update.bind(this));
            Achievement.achievementChangePublisher.unsubscribe(Achievement.Changes.AchievementProgressed, this._update.bind(this));
        },
        _update: function () {
            const list = this.getElementsByTagName('div')[0];
            list.innerHTML = Achievement.achievementList.map(achievement => renderAchievement(achievement)).join('\n');
        }
    });
});