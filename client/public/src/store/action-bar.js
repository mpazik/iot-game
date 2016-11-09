define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const SkillIds = Skills.Ids;

    var publishActive;
    const activeState = new Publisher.StatePublisher(null, function (fn) {
        return publishActive = fn;
    });

    var skills = [];

    var updateSkills = null;
    const skillPosition = [
        SkillIds.PUNCH, SkillIds.BOW_SHOT, SkillIds.SWORD_HIT,
        SkillIds.EAT_APPLE, SkillIds.CREATE_TREE, SkillIds.CUT_TREE,
        SkillIds.CREATE_ARROWS, SkillIds.GRAB_APPLE, SkillIds.INTRODUCE
    ];
    var skillsPublisher = new Publisher.StatePublisher(skills, (push) => {
        updateSkills = push
    });

    Targeting.targetingState.subscribe(function (skill) {
        publishActive(skill == null || skill.type === Skills.Types.BUILD ? null : skill.id);
    });

    module.exports = {
        addSkill(skillKey) {
            const skillId = Skills.keyToId(skillKey);
            const passion = skillPosition.indexOf(skillId);
            skills[passion] = skillId;
            updateSkills(skills.slice())
        },
        skills: skillsPublisher,
        unlockedObjects: [3, 4, 5, 6],
        activeState: activeState
    };
});