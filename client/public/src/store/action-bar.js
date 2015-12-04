define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Targeting = require('../component/targeting');

    var publishActive;
    const activeState = new Publisher.StatePublisher(null, function (fn) {
        return publishActive = fn;
    });

    var skills = [0, 1, 2, null, null, null, null, null, null];

    Targeting.targetingState.subscribe(function (skill) {
        if (skill === null) {
            // skill was deactivated
            if (publishActive.value !== null) {
                // deactivate skill if was activated from action-bar
                publishActive(null);
            }
        } else {
            const index = skills.indexOf(skill.id);
            if (index !== -1) {
                // skill was activated from action-bar
                publishActive(index);
            } else if (publishActive.value !== null) {
                // other skill was activated from other source than action-bar
                publishActive(null);
            }
        }
    });

    module.exports = {
        skills: skills,
        activeState: activeState
    };
});