define(function (require, exports, module) {
    const Pixi = require('pixi');
    const TileSize = require('configuration').tileSize;
    const WorldMap = require('./world');
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Resources = require('../store/resources');
    const WorldMapStore = require('../store/world');
    const WorldObjectStore = require('../store/world-object');

    const lowLayer = new Pixi.Container();
    const highLayer = new Pixi.Container();

    var targetingData = null;

    function recalculateSpritePosition(position) {
        const sprite = targetingData.sprite;
        const tx = Math.floor(position.x - targetingData.spriteOffset.x);
        const ty = Math.floor(position.y - targetingData.spriteOffset.y);
        sprite.position.x = tx * TileSize;
        sprite.position.y = ty * TileSize;

        if (canObjectBeBuild(tx, ty, targetingData.objectKind)) {
            sprite.tint = 0x66FF66;
        } else {
            sprite.tint = 0xFF6666;
        }
    }

    function canObjectBeBuild(startTileX, startTileY, objectKind) {
        function canObjectBeBuildOnTile(tx, ty) {
            return objectKind.terrains.includes(WorldMapStore.terrain(tx, ty));
        }

        for (var i = 0; i < objectKind.width; i++) {
            for (var j = 0; j < objectKind.height; j++) {
                // ct stands for current tile
                var ctx = startTileX + i;
                var cty = startTileY + j;
                if (!canObjectBeBuildOnTile(ctx, cty) || WorldObjectStore.isAnyObjectOnTile(ctx, cty)) {
                    return false;
                }
            }
        }
        return true;
    }

    Targeting.targetingState.subscribe(function (skill) {
        if (targetingData != null) {
            lowLayer.removeChildren();
            highLayer.removeChildren();
            targetingData = null;
            WorldMap.mousePositionStream.unsubscribe(recalculateSpritePosition)
        }

        if (skill == null) return;

        if (skill.type === Skills.Types.BUILDING) {
            const objectKind = Resources.objectKind(skill.worldObject);
            const sprite = Pixi.Sprite.fromImage(objectKind.sprite);
            const spriteOffset = {x: (objectKind.width - 1) * 0.5, y: (objectKind.height - 1) * 0.5};
            targetingData = {objectKind, spriteOffset, sprite};
            recalculateSpritePosition(WorldMap.mousePositionStream.value);
            WorldMap.mousePositionStream.subscribe(recalculateSpritePosition);

            if(objectKind.isHover)
                highLayer.addChild(sprite);
            else
                lowLayer.addChild(sprite);
        }
    });

    module.exports = {
        init: function () {
            highLayer.removeChildren();
        },
        get lowLayer() {
            return lowLayer
        },
        get highLayer() {
            return highLayer
        }
    };
});