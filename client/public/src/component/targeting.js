define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Dispatcher = require('./dispatcher');
    const Skills = require('../common/model/skills');
    const MainPlayer = require('../store/main-player');

    var pushTargeting;
    const targetingState = new Publisher.StatePublisher(null, push => pushTargeting = push);

    const cancelTargetingImmediate = function () {
        pushTargeting(null);
        Dispatcher.userEventStream.unsubscribe('right-click', cancelTargetingImmediate);
        Dispatcher.userEventStream.unsubscribe('left-click', cancelTargetingDeferred);
        Dispatcher.userEventStream.unsubscribe('esc-down', cancelTargetingImmediate);
    };

    const cancelTargetingDeferred = function () {
        // on left click action is performed so the skill in targeting state may be needed by other components.
        deffer(() => pushTargeting(null));
        Dispatcher.userEventStream.unsubscribe('right-click', cancelTargetingImmediate);
        Dispatcher.userEventStream.unsubscribe('left-click', cancelTargetingDeferred);
        Dispatcher.userEventStream.unsubscribe('esc-down', cancelTargetingImmediate);
    };

    function isTargeting() {
        return targetingState.value !== null;
    }

    Dispatcher.userEventStream.subscribe('skill-triggered', function (event) {

        // if there is already some target
        if (targetingState.value !== null) {
            // if the user triggered same skill twice
            if (event.skill.id === targetingState.value.id) {
                cancelTargetingImmediate();

            } else {
                // if the user triggered different skill
                if (isTargetingSkill(event.skill)) {
                    pushTargeting(event.skill);
                } else {
                    cancelTargetingImmediate();
                }
            }
        } else {
            if (isTargetingSkill(event.skill)) {
                pushTargeting(event.skill);
                Dispatcher.userEventStream.subscribe('right-click', cancelTargetingImmediate);
                Dispatcher.userEventStream.subscribe('left-click', cancelTargetingDeferred);
                Dispatcher.userEventStream.subscribe('esc-down', cancelTargetingImmediate);
            }
        }
    });

    var target = null;

    function isTargetingSkill(skill) {
        return skill.type != Skills.Types.CRAFT && skill.type != Skills.Types.USE
    }

    function goToPosition() {
        if (target === null) {
            return
        }

        const userPos = MainPlayer.position;
        if (userPos.isInRange(target, target.action.range)) {
            Dispatcher.messageStream.publish('action-started-on-world-object', {
                action: target.action,
                worldObjectId: target.worldObjectId,
                worldObjectKind: target.worldObjectKind
            });
        } else {
            // move is expensive so it's done every x huntTarget operation
            if (target.attemptToMove == null || target.attemptToMove == 0) {
                target.attemptToMove = 20;
                Dispatcher.userEventStream.publish('move-to', {x: target.x, y: target.y});
            } else {
                target.attemptToMove -= 1;
            }
            setTimeout(goToPosition, 100);
        }
    }

    function stopGoing() {
        target = null;
    }

    Dispatcher.userEventStream.subscribe('world-object-targeted', function (data) {
        target = data;
        goToPosition();
        deffer(() => Dispatcher.userEventStream.subscribeOnce('left-click', stopGoing));
    });

    module.exports = {
        targetingState: targetingState,
        isTargeting
    };
});

