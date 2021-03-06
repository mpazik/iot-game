define(function (require, exports, module) {
    const Pixi = require('pixi');
    const TileSize = require('configuration').tileSize;
    const Targeting = require('../component/targeting');
    const MainPlayer = require('../store/main-player');
    const Skills = require('../common/model/skills');

    const layer = new Pixi.Container();
    const userIndicators = new Pixi.Container();
    var rangeIndicator;
    Targeting.targetingState.subscribe(function (skill) {
        if (rangeIndicator) {
            userIndicators.removeChild(rangeIndicator);
            rangeIndicator = null;
        }

        if (skill == null) return;

        if (skill.type === Skills.Types.ATTACK) {
            rangeIndicator = new Pixi.Graphics();
            rangeIndicator.beginFill(0x74C0FF, 0.2);
            rangeIndicator.lineStyle(3, 0x74C0FF, 0.4);
            const radius = skill.range * TileSize;
            rangeIndicator.drawCircle(0, 0, radius);
            userIndicators.addChild(rangeIndicator)
        }
    });

    module.exports = {
        init: function () {
            layer.removeChildren();
            layer.addChild(userIndicators);
            userIndicators.position = MainPlayer.positionInPixels;
        },
        get layer() {
            return layer
        }
    };
});