define(function (require, exports, module) {
    const Pixi = require('pixi');
    const TileSize = require('configuration').tileSize;
    const SkillStore = require('../store/skill');
    const Point = require('../unit/point');
    const Dispatcher = require('../component/dispatcher');
    const EventIds = require('../common/packet/messages').ids;
    const MainLoop = require('../store/main-loop');
    const MoveStore = require('../store/move');
    const Timer = require('../component/timer');

    const animations = [];
    const projectileSpeed = 20;
    const lengthOfArrow = 1;
    const layer = new Pixi.Container();

    MainLoop.renderStream.subscribe(function (delta) {
        animations.remove(function (animation) {
            const targetPosition = MoveStore.positionAtTime(animation.target, Timer.currentTimeOnServer());
            // speed is in meters per second but delta is in millis.
            const movedDistance = delta * projectileSpeed / 1000;
            const distanceToMove = Point.distance(animation.position, targetPosition);
            const movedDistanceRatio = movedDistance / distanceToMove;
            if (distanceToMove < lengthOfArrow || movedDistanceRatio >= 1) {
                layer.removeChild(animation.projectile);
                return true;
            }
            //noinspection JSPrimitiveTypeWrapperUsage
            animation.position = Point.interpolate(movedDistanceRatio, animation.position, targetPosition);
            animation.projectile.position.x = animation.position.x * TileSize;
            animation.projectile.position.y = animation.position.y * TileSize;
            animation.projectile.rotation = Point.angleFromTo(animation.position, targetPosition);
            return false;
        })
    });

    Dispatcher.messageStream.subscribe(EventIds.SkillUsed, (event) => {
        const skill = SkillStore.skill(event.skillId);
        if (skill.projectile) {
            const position = MoveStore.positionAtTime(event.casterId, Timer.currentTimeOnServer());
            const projectile = new Pixi.Sprite.fromImage(skill.projectile);
            animations.push({projectile: projectile, target: event.targetId, position: position});
            layer.addChild(projectile);
        }
    });

    Dispatcher.messageStream.subscribe(EventIds.CharacterDied, (event) => {
        animations.remove((animation) => animation.target == event.characterId)
    });

    module.exports = {
        layer: layer
    };
});