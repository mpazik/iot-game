define((require) => {
    const Pixi = require('pixi');
    const TileSize = require('configuration').tileSize;
    const parcelSize = require('configuration').parcelSize;
    const zoom = require('configuration').zoom;
    const WorldMap = require('./world');
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Resources = require('../store/resources');
    const WorldObjectStore = require('../store/world-object');
    const WorldMapStore = require('../store/world');
    const Dispatcher = require('../component/dispatcher');
    const Parcel = require('../store/parcel');
    const WorldBoard = require('../render/world-board');

    var targetingData = null;

    function recalculateSpritePosition(position) {
        const sprite = targetingData.sprite;
        const tx = Math.floor(position.x - targetingData.spriteOffset.x);
        const ty = Math.floor(position.y - targetingData.spriteOffset.y);
        sprite.position.x = tx * TileSize;
        sprite.position.y = ty * TileSize;
        WorldBoard.sortDisplayOrder();

        if (canObjectBeBuild(tx, ty, targetingData.objectKind)) {
            sprite.tint = 0x66FF66;
        } else {
            sprite.tint = 0xFF6666;
        }
    }

    function canObjectBeBuild(startTileX, startTileY, objectKind) {
        if (!WorldObjectStore.isFreePlaceForObject(startTileX, startTileY, objectKind)) {
            return false;
        }

        const groundRectangle = WorldObjectStore.getGroundRectangle(startTileX, startTileY, objectKind);
        if (!isObjectOnPlayerParcel(groundRectangle)) {
            return false
        }

        return checkAllTileInRectangle(groundRectangle.x, groundRectangle.y, groundRectangle.width, groundRectangle.height);

        function checkAllTileInRectangle(startX, startY, width, height) {
            // ct stands for current tile
            for (var ctx = startX; ctx < startX + width; ctx++) {
                for (var cty = startY; cty < startY + height; cty++) {
                    if (!canObjectBeBuildOnTile(ctx, cty)) {
                        return false;
                    }
                }
            }
            return true;
        }

        function canObjectBeBuildOnTile(tx, ty) {
            const availableTerrains = targetingData.objectKind['terrains'];
            if (!availableTerrains) {
                return true;
            }
            const terrainIds = WorldMapStore.tileTerrains(tx, ty);
            const terrainNames = terrainIds.map(id => WorldMapStore.terrainName(id));

            return terrainNames.every(terrain => availableTerrains.includes(terrain))
        }

        function isObjectOnPlayerParcel(object) {
            const parcel = Parcel.playerParcel;
            if (!parcel) {
                return false
            }
            const parcelRectangle = {
                x: parcel.x * parcelSize,
                y: parcel.y * parcelSize,
                width: parcelSize,
                height: parcelSize
            };
            return isRectangleInside(parcelRectangle, object)
        }

        function isRectangleInside(r1, r2) {
            return r1.x <= r2.x && r1.x + r1.width >= r2.x + r2.width &&
                r1.y <= r2.y && r1.y + r1.height >= r2.y + r2.height;
        }
    }

    function buildOnPosition(data) {
        const tx = Math.floor(data.x - targetingData.spriteOffset.x);
        const ty = Math.floor(data.y - targetingData.spriteOffset.y);
        if (!canObjectBeBuild(tx, ty, targetingData.objectKind)) return;

        Dispatcher.userEventStream.publish('build-object', {
            objectKindId: targetingData.objectKind.id,
            x: tx,
            y: ty
        })
    }

    Targeting.targetingState.subscribe(function (skill) {
        if (targetingData != null) {
            WorldBoard.removeObject(targetingData.sprite);
            targetingData = null;
            WorldMap.mousePositionStream.unsubscribe(recalculateSpritePosition);
            WorldMap.worldMapClicked.unsubscribe(buildOnPosition);
            Parcel.highlightPlayerParcel(false);
        }

        if (skill == null) return;

        if (skill.type === Skills.Types.BUILD) {
            Parcel.highlightPlayerParcel(true);
            const objectKind = Resources.objectKind(skill.objectKind);
            const sprite = Pixi.Sprite.fromFrame(getSprite());
            sprite.scale = {x: zoom, y: zoom};
            const spriteOffset = {x: (objectKind.width - 1) * 0.5, y: (objectKind.height - 1) * 0.5};
            targetingData = {objectKind, spriteOffset, sprite, skill};
            recalculateSpritePosition(WorldMap.mousePositionStream.value);
            WorldMap.mousePositionStream.subscribe(recalculateSpritePosition);
            WorldMap.worldMapClicked.subscribe(buildOnPosition);
            WorldBoard.addObject(sprite);

            function getSprite() {
                if (objectKind['animationSteps']) {
                    return objectKind['sprite'] + '1' + '.png'
                }
                if (objectKind['growingSteps']) {
                    return objectKind['sprite'] + objectKind['growingSteps'] + '.png'
                }
                return objectKind['sprite'] + '.png'
            }
        }
    });
});