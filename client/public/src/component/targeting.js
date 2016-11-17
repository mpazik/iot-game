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

        if (isInRange(MainPlayer.position, target)) {
            Dispatcher.messageStream.publish('action-started-on-world-object', {
                action: target.action,
                worldObjectId: target.worldObjectId,
                worldObjectKind: target.worldObjectKind
            });
            stopGoing();
        } else {
            // move is expensive so it's done every x huntTarget operation
            if (target.attemptToMove == null || target.attemptToMove == 0) {
                target.attemptToMove = 20;
                Dispatcher.userEventStream.publish('move-to', getDestination(target));
            } else {
                target.attemptToMove -= 1;
            }
            setTimeout(goToPosition, 100);
        }

        function getDestination(target) {
            if (target.width && target.height) {
                return {x: target.x + target.width / 2, y: target.y + target.height + 0.5}
            } else {
                return {x: target.x, y: target.y};
            }
        }

        function isInRange(point, target) {
            if (target.width && target.height) {
                const cx = Math.max(Math.min(point.x, target.x + target.width), target.x);
                const cy = Math.max(Math.min(point.y, target.y + target.height), target.y);
                return point.isInRange({x: cx, y: cy}, target.action.range);
            }
            return point.isInRange(target, target.action.range)
        }
    }

    function stopGoing() {
        target = null;
        Dispatcher.userEventStream.unsubscribe('left-click', stopGoing)
    }

    Dispatcher.userEventStream.subscribe('world-object-targeted', function (data) {
        target = data;
        goToPosition();
        deffer(() => Dispatcher.userEventStream.subscribe('left-click', stopGoing));
    });

    module.exports = {
        targetingState: targetingState,
        isTargeting
    };
});

