define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills').Ids;

    var publishActive;
    const activeState = new Publisher.StatePublisher(null, function (fn) {
        return publishActive = fn;
    });

    var skills = [];

    var updateSkills = null;
    const skillPosition = [
        Skills.PUNCH, Skills.BOW_SHOT, Skills.SWORD_HIT,
        Skills.EAT_APPLE, Skills.CREATE_TREE, Skills.CUT_TREE,
        Skills.CREATE_ARROWS, Skills.GRAB_APPLE, Skills.INTRODUCE
    ];
    var skillsPublisher = new Publisher.StatePublisher(skills, (push) => {
        updateSkills = push
    });

    Targeting.targetingState.subscribe(function (skill) {
        publishActive(skill == null ? null : skill.id);
    });

    function skillKeyToSkillId(skillKey) {
        const skillIdName = skillKey.toUpperCase().replace('-', '_');
        const skillId = Skills[skillIdName];
        if (skillId == null) {
            throw `There is no skill id for a skill ${skillKey}`
        }

        return skillId
    }

    module.exports = {
        addSkill(skillKey) {
            const skillId = skillKeyToSkillId(skillKey);
            const passion = skillPosition.indexOf(skillId);
            skills[passion] = skillId;
            updateSkills(skills.slice())
        },
        skills: skillsPublisher,
        activeState: activeState
    };
});