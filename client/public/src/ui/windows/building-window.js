define(function (require) {
    const Skills = require('../../common/model/skills');
    const skillById = require('../../store/resources').skill;

    function getBuildingSkills() {
        function values(obj) {
            const vals = [];
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    vals.push(obj[key]);
                }
            }
            return vals;
        }

        return values(Skills.Ids).map(skillById).filter(skill => skill.type == Skills.Types.BUILD);
    }

    function renderSkill(skill) {
        return `<div class="skill">
<h3>${skill['title']}</h3>
${renderUnlockDate(achievement)}
<span>${achievement['imperative']}</span>
${renderAchievementProgress(achievement)}
${renderRewards(achievement)}
</div>
`;
    }

    return createUiElement('building-window', {
        type: 'window',
        properties: {
            requirements: {
                playerAlive: Predicates.is(true),
            }
        },
        created: function () {
            this.innerHTML = `
<div>
    
</div>
`;
        },
        attached: function () {
            console.log(getBuildingSkills());
        },
        detached: function () {
        },
        _update: function () {
            this.innerHTML = Skills.achievementList.map(achievement => renderAchievement(achievement)).join('\n');
        }
    });
});