define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Point = require('../unit/point');
    const Dispatcher = require('./dispatcher');
    const Skills = require('../common/model/skills');
    const MainPlayer = require('../store/main-player');
    const MoveStore = require('../store/move');
    const Timer = require('./timer');

    var publishTargeting;
    const targetingState = new Publisher.StatePublisher(null, function (fn) {
        publishTargeting = fn;
    });

    const cancelTargetingImmediate = function () {
        publishTargeting(null);
        Dispatcher.userEventStream.unsubscribe('right-click', cancelTargetingImmediate);
        Dispatcher.userEventStream.unsubscribe('left-click', cancelTargetingDeferred);
    };

    const cancelTargetingDeferred = function () {
        // on left click action is performed so the skill in targeting state may be needed by other components.
        setTimeout(function () {
            publishTargeting(null);
        }, 0);
        Dispatcher.userEventStream.unsubscribe('right-click', cancelTargetingImmediate);
        Dispatcher.userEventStream.unsubscribe('left-click', cancelTargetingDeferred);
    };

    Dispatcher.userEventStream.subscribe('skill-triggered', function (event) {

        // if there is already some target
        if (targetingState.value !== null) {
            // if the user triggered same skill twice
            if (event.skill.id === targetingState.value.id) {
                cancelTargetingImmediate();

            } else {
                // if the user triggered different skill
                publishTargeting(event.skill);
            }
        } else {
            publishTargeting(event.skill);
            Dispatcher.userEventStream.subscribe('right-click', cancelTargetingImmediate);
            Dispatcher.userEventStream.subscribeOnce('left-click', cancelTargetingDeferred);
        }
    });

    var target = null;

    function huntTarget() {
        if (target === null) {
            return
        }
        const characterPos = MoveStore.positionAtTime(target.characterId, Timer.currentTimeOnServer());
        const playerPos = MainPlayer.position;
        const skillRange = target.skillToUse.range;
        if (playerPos.isInRange(characterPos, skillRange)) {
            Dispatcher.userEventStream.publish('skill-used-on-character', {
                characterId: target.characterId,
                skillId: target.skillToUse.id
            });
        } else {
            // move is expensive so it's done every x huntTarget operation
            if (target.attemptToMove === 5) {
                target.attemptToMove = 0;
                const distanceToCreature = playerPos.distanceTo(characterPos);
                const ratio = (distanceToCreature - skillRange) / distanceToCreature;
                // there +10% to make sure that player will be in distance to creature.
                const targetVector = Point.interpolate(ratio * 1.1, playerPos, characterPos);
                Dispatcher.userEventStream.publish('map-clicked', {x: targetVector.x, y: targetVector.y});
            } else {
                target.attemptToMove += 1;
            }
            setTimeout(huntTarget, 100);
        }
    }

    function stopHunting() {
        target = null;
    }

    Dispatcher.userEventStream.subscribe('character-clicked', function (data) {
        const skill = targetingState.value;
        if (skill.type === Skills.Types.ATTACK) {
            target = {
                characterId: data.characterId,
                skillToUse: skill,
                attemptToMove: 5
            };
            huntTarget();
            // deferred because which is invoked by 'left-click' an we want to listen to the next 'left-click'
            setTimeout(function () {
                Dispatcher.userEventStream.subscribeOnce('left-click', stopHunting);
            }, 0);
        } else {
            throw 'Not supported skill type used on creature'
        }
    });

    module.exports = {
        targetingState: targetingState
    };
});

